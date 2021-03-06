(ns ipfs-chain.module.app
  (:require [integrant.core :as ig]
            [reagent.core :as reagent]
            [re-frame.core :as re-frame]
            [day8.re-frame.tracing :refer-macros [fn-traced]]
            [ipfs-chain.views :as views]))

;; Initial DB
(def initial-db {::errors []})

;; Subscriptions
(defmulti reg-sub identity)

;; Events
(defmulti reg-event identity)
(defmethod reg-event ::init [k]
  (re-frame/reg-event-db
   k [re-frame/trim-v]
   (fn-traced
    [db _]
    (merge db initial-db))))
(defmethod reg-event ::halt [k]
  (re-frame/reg-event-db
   k [re-frame/trim-v]
   (fn-traced
    [db _]
    (->> db
         (filter #(not= (namespace (key %)) (namespace ::x)))
         (into {})))))
(defmethod reg-event ::throw-error [k]
  (re-frame/reg-event-db
   k [re-frame/trim-v]
   (fn-traced
    [db [error]]
    (js/console.error error)
    (update-in db [::errors] conj error))))
(defmethod reg-event ::chain-on-ipfs [k]
  (re-frame/reg-event-fx
   k [re-frame/trim-v]
   (fn-traced
    [{:keys [:db]} [hash]]
    (when hash
      (let [path (str "https://ipfs.infura.io/ipfs/" hash)]
        {:db db
         ::redirect {:path path}})))))

;; Effects
(defmulti reg-fx identity)
(defmethod reg-fx ::redirect [k]
  (re-frame/reg-fx
   k (fn [{:keys [:path] :as params}]
       (when path
         (set! js/location.href path)))))

;; Init
(defmethod ig/init-key :ipfs-chain.module/app
  [k {:keys [:mount-point-id]}]
  (js/console.log (str "Initializing " k))
  (let [[subs events effects] (->> [reg-sub reg-event reg-fx]
                                   (map methods)
                                   (map #(map key %)))
        container (.getElementById js/document mount-point-id)]
    (->> subs (map reg-sub) doall)
    (->> events (map reg-event) doall)
    (->> effects (map reg-fx) doall)
    (re-frame/dispatch-sync [::init])
    (when container (reagent/render [views/app-container] container))
    {:subs subs :events events :effects effects :container container}))

;; Halt
(defmethod ig/halt-key! :ipfs-chain.module/app
  [k {:keys [:subs :events :container :effects]}]
  (js/console.log (str "Halting " k))
  (reagent/unmount-component-at-node container)
  (re-frame/dispatch-sync [::halt])
  (->> subs (map re-frame/clear-sub) doall)
  (->> events (map re-frame/clear-event) doall)
  (->> effects (map re-frame/clear-fx) doall))

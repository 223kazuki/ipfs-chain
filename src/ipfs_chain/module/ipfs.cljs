(ns ipfs-chain.module.ipfs
  (:require [integrant.core :as ig]
            [re-frame.core :as re-frame]
            [day8.re-frame.tracing :refer-macros [fn-traced]]
            [cljsjs.ipfs]
            [cljsjs.buffer]))

(def buffer-from (aget js/buffer "Buffer" "from"))

;; Initial DB
(def initial-db {})

;; Subscriptions
(defmulti reg-sub identity)

;; Events
(defmulti reg-event identity)
(defmethod reg-event ::init [k]
  (re-frame/reg-event-db
   k [re-frame/trim-v]
   (fn-traced
    [db [ipfs]]
    (-> db
        (merge initial-db)
        (assoc ::ipfs ipfs)))))
(defmethod reg-event ::halt [k]
  (re-frame/reg-event-db
   k [re-frame/trim-v]
   (fn-traced
    [db _]
    (->> db
         (filter #(not= (namespace (key %)) (namespace ::x)))
         (into {})))))
(defmethod reg-event ::upload-data [k]
  (re-frame/reg-event-fx
   k [re-frame/trim-v]
   (fn-traced
    [{:keys [:db]} [data on-success on-error]]
    {:db db
     ::add {:buffer (buffer-from data)
            :on-success on-success
            :on-error on-error}})))

;; Effects
(defmulti reg-fx identity)
(defmethod reg-fx ::add [k ipfs]
  (re-frame/reg-fx
   k (fn [{:keys [:buffer :on-success :on-error] :as params}]
       (.. (js-invoke ipfs "add" buffer)
           (then (fn [res]
                   (let [hash (aget (first res) "hash")]
                     (when-not (empty? on-success)
                       (re-frame/dispatch (vec (conj on-success hash)))))))
           (catch (fn [err]
                    (when-not (empty? on-error)
                      (re-frame/dispatch (vec (conj on-error err))))))))))

;; Init
(defmethod ig/init-key :ipfs-chain.module/ipfs
  [k opts]
  (js/console.log (str "Initializing " k))
  (let [[subs events effects] (->> [reg-sub reg-event reg-fx]
                                   (map methods)
                                   (map #(map key %)))
        ipfs (js/IpfsApi (clj->js opts))]
    (->> subs (map reg-sub) doall)
    (->> events (map reg-event) doall)
    (->> effects (map #(reg-fx % ipfs)) doall)
    (re-frame/dispatch-sync [::init ipfs])
    {:subs subs :events events :effects effects}))

;; Halt
(defmethod ig/halt-key! :ipfs-chain.module/ipfs
  [k {:keys [:subs :events :effects]}]
  (js/console.log (str "Halting " k))
  (re-frame/dispatch-sync [::halt])
  (->> subs (map re-frame/clear-sub) doall)
  (->> events (map re-frame/clear-event) doall)
  (->> effects (map re-frame/clear-fx) doall))

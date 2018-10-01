(ns ipfs-chain.module.ipfs
  (:require [integrant.core :as ig]
            [re-frame.core :as re-frame]
            [day8.re-frame.tracing :refer-macros [fn-traced]]
            [cljsjs.moment]
            [cljsjs.ipfs]
            [cljsjs.buffer]))

(defn- upload-data [ipfs buffer uploaded-handler]
  (.then (js-invoke ipfs "add" buffer)
         (fn [response]
           (uploaded-handler (aget (first response) "hash")))))

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
(defmethod reg-event ::upload [k]
  (re-frame/reg-event-fx
   k [re-frame/trim-v]
   (fn-traced
    [{:keys [:db]} _]
    (println "!!!!!!!!!!")
    (let [ipfs (::ipfs db)
          buffer (js-invoke js/Buffer "from" "<!DOCTYPE html><html lang=\"en\"><head><meta charset=\"UTF-8\"><title>Document</title></head><body>Test!!!</body></html>")]
      (upload-data ipfs buffer #(println "!!!!!!!!!" %)))
    {:db db})))

;; Init
(defmethod ig/init-key :ipfs-chain.module/ipfs
  [k opts]
  (js/console.log (str "Initializing " k))
  (let [subs (->> reg-sub methods (map key))
        events (->> reg-event methods (map key))
        ipfs (js/IpfsApi (clj->js opts))]
    (->> subs (map reg-sub) doall)
    (->> events (map reg-event) doall)
    (re-frame/dispatch-sync [::init ipfs])
    {:subs subs :events events}))

;; Halt
(defmethod ig/halt-key! :ipfs-chain.module/ipfs
  [k {:keys [:subs :events]}]
  (js/console.log (str "Halting " k))
  (re-frame/dispatch-sync [::halt])
  (->> subs (map re-frame/clear-sub) doall)
  (->> events (map re-frame/clear-event) doall))

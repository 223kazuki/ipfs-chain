(ns cljs.user
  (:require [ipfs-chain.core :refer [system config start stop]]
            [meta-merge.core :refer [meta-merge]])
  (:require-macros [ipfs-chain.utils :refer [read-config]]))

(enable-console-print!)

(println "dev mode")

(swap! config #(meta-merge % (read-config "dev.edn")))

(defn reset []
  (stop)
  (start))

(defn ^:export init []
  (start))

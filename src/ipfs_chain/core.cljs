(ns ipfs-chain.core
  (:require [integrant.core :as ig]
            [ipfs-chain.module.app]
            [ipfs-chain.module.ipfs])
  (:require-macros [ipfs-chain.utils :refer [read-config]]))

(defonce system (atom nil))

(def config (atom (read-config "config.edn")))

(defn start []
  (reset! system (ig/init @config)))

(defn stop []
  (when @system
    (ig/halt! @system)
    (reset! system nil)))

(defn ^:export init []
  (start))

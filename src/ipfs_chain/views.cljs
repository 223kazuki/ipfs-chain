(ns ipfs-chain.views
  (:require [re-frame.core :as re-frame]
            [reagent.core :as reagent]
            [cljsjs.semantic-ui-react]
            [soda-ash.core :as sa]
            [ipfs-chain.module.ipfs :as ipfs]))

(defn- get-meta-data [name]
  (.. js/document
      (querySelector (str "meta[name=" name "]"))
      (getAttribute "content")))

(defn- generate-html []
  (let [root-ipfs-hash (get-meta-data "root-ipfs-hash")
        current-ipfs-hash (-> js/location.pathname
                              (clojure.string/split #"/")
                              last)
        content-root (if-not (empty? root-ipfs-hash)
                       (str "http://localhost:8080/ipfs/" root-ipfs-hash)
                       "http://localhost:3449")]
    (js/console.log root-ipfs-hash)
    (js/console.log current-ipfs-hash)
    (js/console.log content-root)
    (str
     "<!DOCTYPE html>"
     "<html lang=\"en\">"
     "<head>"
     "    <meta charset=\"UTF-8\">"
     "    <title>IPFS Chain</title>"
     "    <meta name=\"root-ipfs-hash\" content=\"" root-ipfs-hash "\">"
     "    <meta name=\"previous-ipfs-hash\" content=\"" current-ipfs-hash "\">"
     "    <meta name=\"viewport\""
     "          content=\"width=device-width,initial-scale=1,shrink-to-fit=no\">"
     "    <link rel=\"stylesheet\" href="
     "          \"//cdnjs.cloudflare.com/ajax/libs/semantic-ui/2.2.13/semantic.min.css\" />"
     "    <link rel=\"stylesheet\" href=\"" content-root "/css/site.css\" />"
     "</head>"
     "<body>"
     "    <div id=\"app\"/>"
     "    <script src=\"" content-root "/js/compiled/app.js\"></script>"
     "    <script>const data=" "</script>"
     "    <script>ipfs_chain.core.init();</script>"
     "</body>"
     "</html>")))

(defn home-panel []
  (let [previous-hash (get-meta-data "previous-ipfs-hash")]
    [:div
     [sa/Button {:on-click
                 #(let [data (generate-html)]
                    (re-frame/dispatch [::ipfs/upload-data data
                                        [:ipfs-chain.module.app/chain-on-ipfs]
                                        [:ipfs-chain.module.app/throw-error]]))}
      "Generate Block"]
     (when-not (empty? previous-hash)
       [sa/Segment
        [:h1 "Previous block"]
        [:a {:href (str "https://ipfs.infura.io/ipfs/" previous-hash)}
         previous-hash]])]))

(defn app-container []
  (fn []
    [:div
     [sa/Menu {:fixed "top" :inverted true}
      [sa/Container
       [sa/MenuItem {:as "span" :header true} "IPFS Chain"]]]
     [sa/Container {:className "mainContainer" :style {:margin-top "7em"}}
      [:div
       [home-panel]]]]))

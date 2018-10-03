(ns ipfs-chain.views
  (:require [re-frame.core :as re-frame]
            [reagent.core :as reagent]
            [cljsjs.semantic-ui-react]
            [cljsjs.react-transition-group]
            [soda-ash.core :as sa]
            [ipfs-chain.module.router :as router]
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
                       (str "https://ipfs.infura.io/ipfs/" root-ipfs-hash)
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
     "    <script>ipfs_chain.core.init();</script>"
     "</body>"
     "</html>")))

(defn home-panel []
  (let [previous-hash (get-meta-data "previous-ipfs-hash")]
    [:div
     [:h1 "Home"]
     [sa/Button {:on-click
                 #(let [data (generate-html)]
                    (re-frame/dispatch [::ipfs/upload-data data
                                        [:ipfs-chain.module.app/chain-on-ipfs]
                                        [:ipfs-chain.module.app/throw-error]]))}
      "Upload"]
     (when-not (empty? previous-hash)
       [sa/Segment
        [:h1 "Previous chain"]
        [:a {:href (str "https://ipfs.infura.io/ipfs/" previous-hash)}
         previous-hash]])]))

(defn about-panel []
  (fn [] [:div "About"]))

(defn none-panel []
  [:div])

(defmulti  panels identity)
(defmethod panels :home-panel [] #'home-panel)
(defmethod panels :about-panel [] #'about-panel)
(defmethod panels :none [] #'none-panel)

(def transition-group
  (reagent/adapt-react-class js/ReactTransitionGroup.TransitionGroup))
(def css-transition
  (reagent/adapt-react-class js/ReactTransitionGroup.CSSTransition))

(defn app-container []
  (let [title (re-frame/subscribe [:ipfs-chain.module.app/title])
        active-panel (re-frame/subscribe [::router/active-panel])]
    (fn []
      [:div
       [sa/Menu {:fixed "top" :inverted true}
        [sa/Container
         [sa/MenuItem {:as "span" :header true} @title]
         [sa/MenuItem {:as "a" :href "/"} "Home"]
         [sa/MenuItem {:as "a" :href "/about"} "About"]]]
       [sa/Container {:className "mainContainer" :style {:margin-top "7em"}}
        (let [panel @active-panel]
          [transition-group
           [css-transition {:key panel
                            :classNames "pageChange" :timeout 500 :className "transition"}
            [(panels panel)]]])]])))

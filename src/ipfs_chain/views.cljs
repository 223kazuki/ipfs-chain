(ns ipfs-chain.views
  (:require [re-frame.core :as re-frame]
            [reagent.core :as reagent]
            [cljsjs.semantic-ui-react]
            [cljsjs.react-transition-group]
            [soda-ash.core :as sa]
            [ipfs-chain.module.router :as router]
            [ipfs-chain.module.ipfs :as ipfs]))

(defn home-panel []
  [:div
   [:h1 "Home"]
   [sa/Button {:on-click #(re-frame/dispatch [::ipfs/upload])} "Upload"]])

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

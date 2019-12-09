(ns scan-img.navbar
  (:require [re-frame.core :as rf]
            [scan-img.subs :as subs]))


(defn nav-bar
  "Generate navigation bar"
  []
  [:div.container
   [:nav.navbar.navbar-dark.bg-primary
    [:span.navbar-text "Docker Image Scanner"]
    [:a {:class "nav-link"
         :href "#"} "Login"]
    [:span.navbar-text "Login"]]])

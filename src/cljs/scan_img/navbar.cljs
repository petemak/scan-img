(ns scan-img.navbar
  (:require [re-frame.core :as rf]
            [scan-img.subs :as subs]))

(def nav-state (atom {}))

(defn nav-bar
  "Generate navigation bar"
  []
  [:div.container
   [:nav.navbar.navbar-expand-lg.navbar-dark.bg-primary
    [:span.navbar-text "Docker Image Scanner"]  
    [:div.collapse.navbar-collapse {:id "navbarText"}
     [:ul.navbar-nav.mr-auto
      [:li.nav-item
       [:a.nav-link {:href "#"} "Login"]]]

     [:div.outerDivFull
      [:div.switchToggle
       [:input {:type "checkbox" :id "upload-type"}]
       [:label {:for "upload-type"} ". Image"]]]
     [:span.navbar-text
      [:a.nav-link {:href "#"} "Login"]]]]])

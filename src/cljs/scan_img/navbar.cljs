(ns scan-img.navbar
  (:require [re-frame.core :as rf]
            [scan-img.subs :as subs]))

;; Current state of navigation
(def nav-state (atom {:view-type :upload-docker-file}))

;; View types
(def view-types [:upload-docker-file :upload-docker-image])

(def view-txt {:upload-docker-file ">>"
               :upload-docker-image "<<"})

;;-----------------------------------------------------
;; Toggle view type
;;-----------------------------------------------------
(defn switch-view
  "Switches to next view type in view-types array"
  []
  (let [idx (.indexOf view-types (:view-type @nav-state))
        nidx (if (>= (inc idx) (count view-types)) 0 (inc idx))]
    (swap! nav-state assoc :view-type (get view-types nidx)))
  (:view-type @nav-state))


;;--------------------------------------------------------------------------
;; Handle click on shwitc button
;;--------------------------------------------------------------------------
(defn handle-switch-btn
  "Handler for switcher button"
  [e]
  (.preventDefault e)
  (let [nv (switch-view)]
    (rf/dispatch [:view-type nv])))

(defn nav-bar
  "Generate navigation bar"
  []
  (let [view-type @(rf/subscribe [:view-type])
        btn-txt (get view-txt view-type ">>")]
     [:div.container
     [:nav.navbar.navbar-expand-lg.navbar-dark.bg-primary
      [:span.navbar-text "Docker Image Scanner"]
      [:button.navbar-toggler {:type "button"
                               :data-toggle "collapse"
                               :data-content "#navbarContents"
                               ;;:aria-content "navbarContents"
                               :aria-expanded "false"
                               :aria-label "Toggle navidation"}
       [:span.navbar-toggler-icon]]
      [:div.collapse.navbar-collapse {:id "navbarContents"}
       [:ul.navbar-nav.mr-auto
        [:li.nav-item.active
         [:a.nav-link {:href "#"} ""]]]
       [:form.form-inline.my-2.my-lg-0
        [:button.btn-outline-danger.my-2.my-sm-0 {:type "submit"
                                                  :on-click #(handle-switch-btn %)} btn-txt]]]]]))

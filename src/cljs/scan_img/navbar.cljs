(ns scan-img.navbar
  (:require [re-frame.core :as rf]
            [scan-img.subs :as subs]))


;;--------------------------------------------------------------------------
;; Maps view keys to required event
;;--------------------------------------------------------------------------
(def view-type-map {:image-view :upload-docker-image
                    :file-view :upload-docker-file
                    :config-view :edit-config})



;;--------------------------------------------------------------------------
;; Diptach view type event
;;--------------------------------------------------------------------------
(defn dispatch-event
  "Dispatches a view event mapped to the specified key"
  [ek]
  (rf/dispatch [:view-type (get view-type-map ek)]))


(defn view-type-is
  "Check if the view id represents the current view type"
  [vid current-vtype]
  (= (get view-type-map vid) current-vtype))


;;--------------------------------------------------------------------------
;; Diptach view type event
;;--------------------------------------------------------------------------
(defn nav-bar
  "Generate navigation bar"
  []
  (fn []    
    (let [view-type @(rf/subscribe [:view-type])]
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
         [:div.btn-group.btn-group-sm {:role "group"}
          [:button  {:class (if (view-type-is :image-view view-type) "btn btn-secondary" "btn btn-success")
                     :on-click #(dispatch-event :image-view)}[:i {:class "fab fa-docker"}] " Image"]
          
          [:button {:class (if (view-type-is :file-view view-type) "btn btn-secondary" "btn btn-warning")
                    :on-click #(dispatch-event :file-view)}  [:i {:class "fas fa-code"}] " Code"]
          
          [:button {:class (if (view-type-is :config-view view-type) "btn btn-secondary" "btn btn-danger")
                    :on-click #(dispatch-event :config-view)} [:i {:class "fas fa-edit"}] " Config"]]]]])))

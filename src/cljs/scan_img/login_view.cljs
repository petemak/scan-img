(ns scan-img.login-view
  (:require [cljs.reader :as cr]
            [reagent.core :as r]
            [re-frame.core :as rf]
            [scan-img.subs :as subs]
            [scan-img.utils :as utils]
            [ajax.core :refer [POST]]))


(defn reset-clicked
  "Event handler for resetting content"
  [])




;;--------------------------------------------------------------
;; Action handler invoked when  submitt button is pressed to
;;--------------------------------------------------------------
(defn submit-clicked
  "Action handler for submitting code, user-name and password"
  [s]
  (println "::submit-clicked: " @s ": user-id " (:user-id @s)))


(defn login-form
  []
  (let [state (r/atom {:user-id "werr" :password ""})]
    (fn []
      [:div {:class "login-form"}
       [:form {:id "login-form" :class "was-validated" :on-submit (fn [e]
                                                                   (.preventDefault e))}

        [:h3 {:class "text-center"} "Login"]
        [:div {:class "form-group"}
         [:label {:for :user-id} "User Id:"]
         [:input {:id :user-id 
                  :type :text
                  :class "form-control"
                  :value (:user-id @state)
                  :on-change (fn [e]
                               (swap! state assoc :user-id (-> e .-target .-value)))}]]
        [:div {:class "form-group"}             
         [:label {:for "password"} "Password:"] 
         [:input {:id "password"
                  :type :password
                  :class "form-control"
                  :value (:password @state)
                  :on-change (fn [e]
                               (swap! state assoc :password (-> e .-target .-value)))}]]
        
        [:hr]

        [:div {:class "form-group"}
         [:button {:type "reset"
                   :class "btn btn-danger float-left"
                   :on-click #(reset-clicked)} [:i {:class "fas fa-eraser"}] " Reset"]
         [:button {:type :submit
                   :class "btn btn-primary float-right"
                   :on-click #(submit-clicked state)} [:i {:class "fas fa-sign-in-alt"}] " Login"]]]])))

(ns scan-img.code-view
  (:require [cljs.reader :as cr]
            [reagent.core :as r]
            [re-frame.core :as rf]
            ;; [day8.re-frame.http-fx] ;; will cause :http-xhrio to register with re-frame
            [scan-img.utils :as utils]
            [scan-img.subs :as subs]))


(defn reset-form
  []
  (rf/dispatch [:reset-form]))


;;--------------------------------------------------------------
;; Action handler invoked when  submitt button is pressed to
;;--------------------------------------------------------------
(defn submit-clicked
  "Action handler for submitting code, user-name and password"
  []
  (println "::--> text-input - submitting: ....")
    ;; (swap! tick-status assoc :tick true)
  (rf/dispatch [:submit-clicked]))



;;--------------------------------------------------------------
;; creates an input field based on information provided for name
;; type, etc
;;--------------------------------------------------------------
(defn input-field
  "Ceates and input field component"
  [id nam typ lbl val on-chg req]
  [:div {:class "form-group col"}
   [:label {:for id} lbl]   
   [:input {:id id
            :type typ
            :class "form-control"
            :value @val
            :on-change on-chg
            :required req}]])



;;--------------------------------------------------------------
;; Main text file and user-name pasword filed component. 
;; 
;;--------------------------------------------------------------
(defn text-field
  "Create text input area and password fields"
  []
  (let [val-unm  (rf/subscribe [:user-name])
        val-pwd  (rf/subscribe [:password])
        on-chg-unm #(rf/dispatch [:modify-name (-> % .-target .-value)])
        on-chg-pwd #(rf/dispatch [:modify-password (-> % .-target .-value)])]
    (fn [] 
      [:div
       [:form {:id "text-field" :class "was-validated" :on-submit (fn [e]
                                                                    (.preventDefault e))}
        [:div {:class "form-group"}
         [:label {:for :code-txt-field} "Paste Docker file"]
         [:textarea {:id :code-txt-field
                     :name :code-txt-field
                     :class "form-control"
                     :rows 10
                     :value @(rf/subscribe [:code-text])
                     :on-change #(rf/dispatch [:modify-code (-> % .-target .-value)])
                     :required "true"}]
         
         [:div {:class "invalid-feedback"} "Ensure you enter valid Docker code that generates ephemeral containers"]]
        [:div {:class "row"}
         [input-field :user-name :user-name nil         "User Name" val-unm on-chg-unm true]
         [input-field :password  :password  "password"  "Password"  val-pwd on-chg-pwd true]]
        
        [:hr]
        [:div {:class "form-group"}
         [:button {:type "reset"
                   :class "btn btn-danger float-left"
                   :on-click #(reset-form)} "Clear"]
         
         [:button {:type :submit
                   :class "btn btn-primary float-right"
                   :disabled @(rf/subscribe [:submit-disabled?])
                   :on-click #(submit-clicked)} "Scan..."]]]])))

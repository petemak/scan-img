load-them(ns scan-img.text-input
  (:require [cljs.reader :as cr]
            [reagent.core :as r]
            [re-frame.core :as rf]
            ;; [day8.re-frame.http-fx] ;; will cause :http-xhrio to register with re-frame
            [scan-img.subs :as subs]))


(defn reset-form
  [])

(defn input-field
  [id nam typ lbl plh val on-chg]
  [:div {:class "form-group col"}
   [:label {:for id} lbl]   
   [:input {:id id
            :type typ
            :class "form-control"
            :placeholder plh
            :on-change on-chg}]])


(defn submit-code
  []
  (let [code @(rf/subscribe [:code-text])
        name @(rf/subscribe [:user-name])
        pswd @(rf/subscribe [:password])]
    (println "::--> submitting: " code)
    (rf/dispatch [:submit-code-text {:code code :name name :password pswd}])))



(defn text-field
  "Create text input area"
  []
  (let [on-chg-unm #(rf/dispatch [:username-change (-> % .-target .-value)])
        on-chg-pwd #(rf/dispatch [:password-change (-> % .-target .-value)])]
    (fn [] 
      [:div
       [:form {:id "text-field" :on-submit (fn [e]
                                             (.preventDefault e))}
        [:div {:class "form-group"}
         [:label {:for :code-txt-field} "Paste Docker file"]
         [:textarea {:id :code-txt-field
                     :name :code-txt-field
                     :class "form-control"
                     :rows 10
                     :on-change #(rf/dispatch [:code-text-change (-> % .-target .-value)])
                     ;; :value (rf/subscribe [:code-txt])
                     
                     } ]]

        [:div {:class "row"}
         [input-field :user-name :user-name nil         "User Name" "User id"  "user name ..." on-chg-unm]
         [input-field :password  :password  "password"  "Password"  "Password"  nil            on-chg-pwd]]
        
        [:hr]
        [:div {:class "form-group"}
         [:button {:type "reset"
                   :class "btn btn-danger float-left"
                   :on-click #(reset-form)} "Clear"]
         
         [:button {:type :submit
                   :class "btn btn-primary float-right"
                   :on-click #(submit-code)} "Scan..."]]]])))

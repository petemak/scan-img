(ns scan-img.text-input
  (:require [cljs.reader :as cr]
            [re-frame.core :as rf]
            [scan-img.subs :as subs]))


(defn reset-form
  [])


(defn submit-code
  [])


(defn text-field
  []
  [:div
   [:form {:id "text-field"}
    [:div {:class "form-group"}
     [:label {:for "code-txt-field"} "Paste docker description code here"]
     [:textarea {:id "code-txt-field" :class "form-control" :rows 10}]]]
   [:hr]
   [:div {:class "form-group"}
     [:button {:type "reset"
               :class "btn btn-danger float-left"
               :on-click #(reset-form)} "Reset"]
     
     [:button {:type "button"
               :class "btn btn-primary float-right"
               :on-click #(submit-code)} "Scan..."]]])

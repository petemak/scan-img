(ns scan-img.text-input
  (:require [cljs.reader :as cr]
            [reagent.core :as r]
            [re-frame.core :as rf]
            [scan-img.subs :as subs]))


(defn reset-form
  [])


(defn submit-code
  []
  (println "::-> submit-code ............"))


(defn text-field
  "Create text input area"
  []
  (let [lst (r/atom {:text ""})]
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
                     :on-change (fn [e]
                                  (swap! lst :text (-> e
                                                       .-target
                                                       .-value)))}
          (:text-field @lst)]]        
        [:hr]
        [:div {:class "form-group"}
         [:button {:type "reset"
                   :class "btn btn-danger float-left"
                   :on-click #(reset-form)} "Clear"]
         
         [:button {:type :submit
                   :class "btn btn-primary float-right"
                   :on-click #(submit-code)} "Scan..."]]]])))

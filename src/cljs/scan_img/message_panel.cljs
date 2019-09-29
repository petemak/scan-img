(ns scan-img.message-panel
  (:require [re-frame.core :as rf]))


(defn message-view
  [msg]
  [:div {:class "alert alert-info alert-dismissible fade show"}
   [:h5 {:class "alert-heading"} "Command: " (:command msg)]
   [:br]
   [:p "Results: " (:message msg)]
   [:button {:type "button" :class "close" :data-dismiss "alert"} "x"]])


(defn messages-view
  []
  (let [messages @(rf/subscribe [:upload-status])]
    [:div
     (map message-view (:results messages))]))

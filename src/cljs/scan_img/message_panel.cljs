(ns scan-img.message-panel
  (:require [re-frame.core :as rf]))



;;--------------------------------------------------------------
;; Run commands. 
;; side effects!!!
;;--------------------------------------------------------------
" The return map contains a list of maps mapped to the key :results
  Each map in the list contains the executed command and results
  {:results [ {:command \"bla bla\"
               :message \"Message...\"
               :outstrlst \"exception...\"}
              {:command ...
               :message ...
               outstrlst ...}  ]}"
(defn message-view
  [msg]
  [:div {:class "alert alert-info alert-dismissible fade show"}
   [:h5 {:class "alert-heading"} "Command: " (:command msg)]
   [:br]
   [:p "Results: " (:message msg)]
   [:br]
   [:p "Detalis: " (:outstrlst msg)]

   
   [:button {:type "button" :class "close" :data-dismiss "alert"} "x"]])


(defn messages-view
  []
  (let [messages @(rf/subscribe [:upload-status])]
    [:div
     (map message-view (:results messages))]))

(ns scan-img.message-panel
  (:require [re-frame.core :as rf]
            [chord.client :refer [ws-ch]]
            [cljs.core.async :refer [<! >! put! close!]]))



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
  (println "::-> message: " msg)
  [:div {:class "alert alert-info alert-dismissible fade show"}
   [:h5 {:class "alert-heading"} "Command: " (:command msg)]
   [:br]
   [:p "Results: " (:message msg)]
   [:br]
   [:p "Details: " (:outstrlst msg)]

   
   [:button {:type "button" :class "close" :data-dismiss "alert"} "x"]])


(defn messages-view
  "Expects a collection of upload status  messages from a subscribed channel
   and creates panels"
  []
  (let [messages @(rf/subscribe [:upload-status])]
      ;; either there is a a list of message maps or a single map with a :results key pointing to
      ;; a list of maps:
      ;;
      ;; cmd-messages is a map with a list mapped to the key
      ;; {:results [{:command "...."
      ;;             :message "..."
      ;;             :outstrlst "..."}]}
    ;;
    (fn [])
      (if (some? (:results messages))
        [:div 
         (map message-view (:results messages))])))

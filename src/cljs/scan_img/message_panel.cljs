(ns scan-img.message-panel
  (:require [re-frame.core :as rf]
            [chord.client :refer [ws-ch]]
            [cljs.core.async :refer [<! >! put! close!]]))



(defn list-to-ul
  "Create a UL from a list"
  [strlst]
  [:ul {:class "list-unstyled"}
    (for [st strlst]
      [:li {:id (subs st 0 5)} st])]  )

(defn map-to-ul
  "Create a UL from a map"
  [strlst]
  [:ul {:class "list-unstyled"}
    (for [st (vals strlst)]
      [:li {:id (subs st 0 5)} st])]  )


(defn outstr->str
  "Create a string from the specified value"
  [outstr]
  (cond
    (instance? cljs.core/PersistentVector outstr) (list-to-ul outstr)
    (instance? cljs.core/List outstr) (list-to-ul outstr)
    (instance? cljs.core/PersistentArrayMap outstr) (map-to-ul outstr)
    :else [:p outstr]))


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
  (println "::--> message-view message: " msg)
  (println "::--> type msg: " (type msg))
  [:div {:class "alert alert-info alert-dismissible fade show"}
   [:button {:type "button" :class "close" :data-dismiss "alert"} "x"] 
   [:h5 {:class "alert-heading"} "Command: " (:command msg)]
   [:br]
   [:p "Results: " (:message msg)]

   (when (:outstrlst msg) 
     [:hr]
     (outstr->str (:outstrlst msg)))])


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

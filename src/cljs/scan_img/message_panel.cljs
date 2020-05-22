(ns scan-img.message-panel
  (:require [re-frame.core :as rf]
            [chord.client :refer [ws-ch]]
            [clojure.string :as str]
            [scan-img.utils :as utils]
            [cljs.core.async :refer [<! >! put! close!]]))



;;--------------------------------------------------------------
;; Utility: creates a UL from a string list
;;--------------------------------------------------------------
(defn list-to-ul
  "Create a UL from a list"
  [strlst]
  (if (not (empty? strlst))
    [:ul {:class "list-unstyled"}
     (for [st strlst]
       [:li {:id (utils/unique-key st)} st])]))


;;--------------------------------------------------------------
;; Utility: creates a string from a string list
;;--------------------------------------------------------------
(defn list-to-str
  "Create a UL from a list"
  [lst]
  (apply str (interpose " " lst)))


;;--------------------------------------------------------------
;; Utility: created a UL frpm a map 
;;--------------------------------------------------------------
(defn map-to-ul
  "Create a UL from a map"
  [strlst]
  [:ul {:class "list-unstyled"}
    (for [st (vals strlst)]
      [:li {:id (utils/unique-key st)} st])])

;;--------------------------------------------------------------
;; Utility: creates a string from a string list
;;--------------------------------------------------------------
(defn map-to-str
  "Create a UL from a list"
  [mp]
  [:ul {:class "list-unstyled"}
   (for [k (keys mp)]
     [:li {:id (utils/unique-key  (get mp k))} (get mp k)] )])



;;--------------------------------------------------------------
;; Utility: create UL from collection types
;;--------------------------------------------------------------
(defn outstr->str
  "Create a string from the specified value"
  [outstr]
  (cond
    (instance? cljs.core/PersistentVector outstr) (list-to-ul outstr)
    (instance? cljs.core/List outstr) (list-to-ul outstr)
    (instance? cljs.core/PersistentArrayMap outstr) (map-to-ul outstr)
    :else [:p (str outstr)]))


;;--------------------------------------------------------------
;; Utility: create UL from collection types
;;--------------------------------------------------------------
(defn list->str
  "Create a string from the specified value"
  [lst]
  (cond
    (instance? cljs.core/PersistentVector lst) (list-to-str lst)
    (instance? cljs.core/List lst) (list-to-str lst)
    (instance? cljs.core/PersistentArrayMap lst) (map-to-str lst)
    :else (str lst)))



;;--------------------------------------------------------------
;; Run commands. 
;; side effects!!!
;;--------------------------------------------------------------
(defn message-view
 " The return map contains a list of maps mapped to the key :results
  Each map in the list contains the executed command and results
  {:results [ {:command \"bla bla\"
               :message \"Message...\"
               :outstrlst \"exception...\"}
              {:command ...
               :message ...
               outstrlst ...}  ]}"  
  [msg]
  (println "::==> message-panel/message-view message: " msg)
  (println "::==> message-panel/message-view type msg: " (type msg))
  (println "::==> message-panel/message-view type command: " (type (:command msg)))
  (println "::==> message-panel/message-view msg value: " (:message msg))
  (println "::==> message-panel/message-view command value: " (:command msg))
  [:div {:class "alert alert-info alert-dismissible fade show"
         :key (utils/unique-key nil)}
   [:button {:type "button" :class "close" :data-dismiss "alert"} "x"] 
   [:h5 {:class "alert-heading"} "Command: " (list->str (:command msg))]
   [:br]
   [:p "Results: "
    (when (:message msg)
       (outstr->str (:message msg)))]

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
      (println "::==> message-panel/messages-view message: " messages)    
      (if (some? (:results messages))
        [:div 
         (map message-view (:results messages))])))

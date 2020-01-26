(ns scan-img.views
  (:require [re-frame.core :as rf]
            [scan-img.navbar :as nv]
            [scan-img.subs :as subs]
            ;;[paren-soup.core :as ps]
            [scan-img.utils :as utils]
            [scan-img.confedit :as ced]
            [scan-img.code-view :as cdv ]
            [scan-img.image-view :as imv]
            [scan-img.message-panel :as msg]
            [scan-img.progress-bar :as pbar]))





;;-----------------------------------------------
;; Initialise paren-soup
;;-----------------------------------------------
;;(ps/init-all)



;;-----------------------------------------------------------
;; Status nndicator
;;----------------------------------------------------------
(defn status-indicator []
  (let [status @(rf/subscribe [:upload-status])]
    (println "::--> Status --[" status "]--")

    
    [:div
     [:div {:class "alert alert-success" :role "alert"}
      [:h4 (:title status)]
      [:ul
       (for [msg (:upl-messages status)]
         [:li {:key (utils/unique-key nil)} msg])]

      (if (some? (:cmd-messages status))
        [:h4 "Scan results"])

      (let [cmd-messages (:cmd-messages status)
            output-msg (:message cmd-messages)
            output-list (:outstrlst cmd-messages)]
           
      
        (if (some? cmd-messages)
          [:ul
           [:li {:key "msg"}  (str ":message " (:message cmd-messages))]
           [:li {:key "exit"}  (str ":exit " (:exit cmd-messages))]
           [:li {:key "err"} (str ":err " (:err cmd-messages))]
           (for [out output-list]
             [:li {:key (utils/unique-key nil)} out])]))]]))


;;----------------------------------------------------------
;; Main view and entry
;;----------------------------------------------------------
(defn main-panel
  []
  [:div.container
   [nv/nav-bar]
   [:br]
   [:div.container.conatiner_fluid
    [:div.row
     (let [view-type @(rf/subscribe [:view-type])]
       (cond
         (= :upload-docker-image view-type) [:div.col [imv/upload-form]] 
         (= :upload-docker-file view-type)  [:div.col [cdv/text-field]]
         (= :edit-config view-type)         [:div.col [ced/editor]]
         :else                              [:div.col [cdv/text-field]]))]      
    [:hr]
    [:div.row
     [:div.col [pbar/progress-bar]]]
    [:br]
    [:div.row
     [:div.col [msg/messages-view]]]]])


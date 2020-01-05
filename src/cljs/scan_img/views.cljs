(ns scan-img.views
  (:require 
            [re-frame.core :as rf]
            [scan-img.navbar :as nv]
            [scan-img.subs :as subs]
            [scan-img.utils :as utils]
            [scan-img.code-view :as cdv ]
            [scan-img.image-view :as imv]
            [scan-img.message-panel :as msg]
            [ajax.core :refer [POST]]))



(defonce tick-status (atom {:tick false}))

;;----------------------------------------------------------
;; Ticker event to drive progress monitor
;;----------------------------------------------------------
(defn dispatch-tick
  "Dispatch ticking event"
  []
  (if (:tick (deref tick-status))
    (rf/dispatch [:progress-tick])))


;; call the ticker dispatching function every half a second
(defonce ticker (js/setInterval dispatch-tick 400))


;;-----------------------------------------------------------
;; Progress bar
;;----------------------------------------------------------
(defn progress-bar []
  (let [tick   @(rf/subscribe [:progress-tick])
        submitting?  @(rf/subscribe [:submitting-data?])        
        done?  @(rf/subscribe [:done-submitting-data?])

        tick2  (if done? "100" tick)
        ptick (str tick2 "%")]
    (println "::--> progress-bar tick:  --[" tick "]--")
    (println "::--> progress-bar state --[" done? "]--")
    (println "::--> progress-bar tick2 --[" tick2 "]--")

    (if submitting?
      (swap! tick-status assoc :tick true)
      (swap! tick-status assoc :tick false))
    
    [:div
     [:div {:class "progress"}
      [:div {:class ["progress-bar" "progress-bar-striped" "bg-success"]
             :role "progressbar"
             :style {:width ptick}
             :aria-valuenow tick2  
             :aria-valuemin "0"
             :aria-valuemax "100"} ptick]]]))



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
         [:li {:key (subs msg 0 2)} msg])]

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
             [:li {:key (subs out 0 2)} out])]))]]))


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
       (if (= view-type :upload-docker-image) 
         [:div.col [imv/upload-form]] 
         [:div.col [cdv/text-field]]))]      
    [:hr]
    [:div.row
     [:div.col [progress-bar]]]
    [:br]
    [:div.row
     [:div.col [msg/messages-view]]]]])




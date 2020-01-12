(ns scan-img.progress-bar
  (:require [re-frame.core :as rf]
            [scan-img.subs :as subs]
            [scan-img.utils :as utils]))



;;----------------------------------------------------------
;; Ticker event to drive progress monitor
;; Tick step is 5
;;----------------------------------------------------------
(defn dispatch-tick
  "Dispatch ticking event"
  []
  (rf/dispatch [:progress-bar/tick 5]))


;;-----------------------------------------------------------
;; call the ticker dispatching function every half a second
;;-----------------------------------------------------------
(defonce ticker (js/setInterval dispatch-tick 500))



;;-----------------------------------------------------------
;; Progress bar
;;----------------------------------------------------------
(defn progress-bar []
  
  (let [val @(rf/subscribe [:progress-bar/actual-value])        
        pval (str val "%")]
    [:div
     [:div {:class "progress"}
      [:div {:class ["progress-bar" "progress-bar-striped" "bg-success"]
             :role "progressbar"
             :style {:width pval}
             :aria-valuenow val  
             :aria-valuemin "0"
             :aria-valuemax "100"} pval]]]))




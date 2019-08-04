(ns scan-img.events
  (:require
   [re-frame.core :as rf]
   [scan-img.db :as db]))

(rf/reg-event-db
 ::initialize-db
 (fn [_ _]
   db/default-db))


;;-----------------------------------------------------------
;; Domino 2: comupte effect of name change event
;;-----------------------------------------------------------
(rf/reg-event-fx
 :name-change
 (fn [{:keys [db]} [_ new-name]]
   {:db (assoc db :name new-name)}))


;;-----------------------------------------------------------
;; Domino 2: comupte effect of upload status event
;;-----------------------------------------------------------
(rf/reg-event-fx
 :upload-status
 (fn [{:keys [db]} [_ new-status]]
   {:db (assoc db :upload-status new-status)}))



;;-----------------------------------------------------------
;; Domino 2: comupte effect of selecting a file
;;-----------------------------------------------------------
(rf/reg-event-fx
 :file-selected
 (fn [{:keys [db]} [_ file-name]]
   {:db (assoc db :file-selected file-name)}))


;;-----------------------------------------------------------
;; Domino 2: comupte effect of ticker
;;-----------------------------------------------------------
(rf/reg-event-fx
 :progress-tick
 (fn [{:keys [db]} [_ _]]
   (let [tick (:progress-tick db)
         new-tick (if (< tick 100) (+ tick 10) 0)] 
     {:db (assoc db :progress-tick new-tick)})))






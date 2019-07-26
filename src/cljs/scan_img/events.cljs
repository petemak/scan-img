(ns scan-img.events
  (:require
   [re-frame.core :as rf]
   [scan-img.db :as db]))

(rf/reg-event-db
 ::initialize-db
 (fn [_ _]
   db/default-db))

(rf/reg-event-fx
 :name-change
 (fn [{:keys [db]} [_ new-name]]
   {:db (assoc db :name new-name)}))

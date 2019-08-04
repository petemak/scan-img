(ns scan-img.subs
  (:require
   [re-frame.core :as rf]))

(rf/reg-sub
 ::name
 (fn [db]
   (:name db)))


(rf/reg-sub
 :upload-status
 (fn [db]
   (:upload-status db)))


(rf/reg-sub
 :file-selected
 (fn [db]
   (:file-selected db)))


(rf/reg-sub
 :progress-tick
 (fn [db]
   (str (:progress-tick db))))

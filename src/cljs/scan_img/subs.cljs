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

;;-----------------------------------------------------------
;; Domino 4: querry effect of progressing the ticker
;;-----------------------------------------------------------
(rf/reg-sub
 :progress-tick
 (fn [db]
   (str (:progress-tick db))))


;;-----------------------------------------------------------
;; Domino 4: querry effect of resetting the ticker
;;-----------------------------------------------------------
(rf/reg-sub
 :reset-ticker
 (fn [db]
   (str (:progress-tick db))))


;;-----------------------------------------------------------
;; Domino 4: querry effect of resetting the form
;;-----------------------------------------------------------
(rf/reg-sub
 :reset-form
 (fn [db]
   (str (:file-selected db))))

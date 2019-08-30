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


;;-----------------------------------------------------------
;; Domino 4: querry effect of changing the upload type
;;-----------------------------------------------------------
(rf/reg-sub
 :upload-type
 (fn [db]
   (:upload-type db)))


;;-----------------------------------------------------------
;; Domino 4: querry effect of of changing the code
;;-----------------------------------------------------------
(rf/reg-sub
 :code-text
 (fn [db]
   (:code-text db)))



;;-----------------------------------------------------------
;; Domino 4: querry effect of of changing the user name
;;-----------------------------------------------------------
(rf/reg-sub
 :user-name
 (fn [db]
   (:user-name db)))





;;-----------------------------------------------------------
;; Domino 4: querry effect of of changing the user password
;;-----------------------------------------------------------
(rf/reg-sub
 :password
 (fn [db]
   (:password db)))

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
   #_(:submission-results db)
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
;; Domino 4: querry effect of activating the ticker - 2
;;-----------------------------------------------------------
(rf/reg-sub
 :progress-bar/ticker-switch
 (fn [db]
   (:progress-bar/ticker-switch db)))



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



;;-----------------------------------------------------------
;; Domino 4: querry status fo disabling the upload button
;;           
;;-----------------------------------------------------------
(rf/reg-sub
 :submit-disabled?
 (fn [db _]
   (not=  (:state db) :READY)))


;;-----------------------------------------------------------
;; Domino 4: querry status
;;           
;;-----------------------------------------------------------
(rf/reg-sub
 :submitting-data?
 (fn [db _]
   (= (:state db) :SUBMITTING-DATA)))


;;-----------------------------------------------------------
;; Domino 4: querry status
;;           
;;-----------------------------------------------------------
(rf/reg-sub
 :done-submitting-data?
 (fn [db]
   (or  (= (:state db) :SUCCESS)
        (= (:state db) :ERROR-RESPONSE))))




;;-----------------------------------------------------------
;; Domino 4: querry effect of a successful submitting code
;;-----------------------------------------------------------
(rf/reg-sub
 :successful-code-req
 (fn [db]
   (:code-text-results db)))



;;-----------------------------------------------------------
;; Domino 4: querry effect of a failed submitting code
;;-----------------------------------------------------------
(rf/reg-sub
 :failed-code-req
 (fn [db _]
   (:code-text-error db)))


;;-----------------------------------------------------------
;; Domino 4: querry effect of setting view type
;;-----------------------------------------------------------
(rf/reg-sub
 :view-type
 (fn [db _]
   (:view-type db)))

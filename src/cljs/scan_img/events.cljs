(ns scan-img.events
  (:require
   [scan-img.db :as db]
   [ajax.core :as ajax]
   [re-frame.core :as rf]
   [day8.re-frame.http-fx])) ;; wil cause :http-xhrio to reister with re-frame

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
         new-tick (if (< tick 100) (+ tick 5) 0)] 
     {:db (assoc db :progress-tick new-tick)})))



;;-----------------------------------------------------------
;; Domino 2: comupte effect of resetting the ticker
;;-----------------------------------------------------------
(rf/reg-event-fx
 :reset-ticker 
 (fn [{:keys [db]} [_ val]]
   {:db (assoc db :progress-tick val)}))



;;-----------------------------------------------------------
;; Domino 2: comupte effect of resetting the form
;;-----------------------------------------------------------
(rf/reg-event-fx
 :reset-form 
 (fn [{:keys [db]} [_ _]]
   (let [ndb (assoc db :file-selected "")]
     {:db (assoc ndb :progress-tick 0)})))


;;-----------------------------------------------------------
;; Domino 2: comupte effect of selecting the upload type 
;;-----------------------------------------------------------
(rf/reg-event-fx
 :upload-type 
 (fn [{:keys [db]} [_ val]] 
   {:db (assoc db :upload-type val)}))


;;-----------------------------------------------------------
;; Domino 2: comupte effect of selecting the upload type 
;;-----------------------------------------------------------
(rf/reg-event-fx
 :code-text-change 
 (fn [{:keys [db]} [_ val]]
   {:db (assoc db :code-text val)}))


;-----------------------------------------------------------
;; Domino 2: comupte effect of entering or changing the user name
;;-----------------------------------------------------------
(rf/reg-event-fx
 :username-change 
 (fn [{:keys [db]} [_ val]]
   {:db (assoc db :user-name val)}))


;;-----------------------------------------------------------
;; Domino 2: comupte effect of entering or changing the
;;-----------------------------------------------------------
(rf/reg-event-fx
 :password-change 
 (fn [{:keys [db]} [_ val]]
   {:db (assoc db :password val)}))



;;-----------------------------------------------------------
;; Domino 2: comupte effect of submitting using :http-xhrio
;;-----------------------------------------------------------
(rf/reg-event-fx
 :submit-code-text 
 (fn [{:keys [db]} [_ val]]
   (println "::--> submit-code-txt: " val)
   {:db (assoc db :show-progress-bar true)
    :http-xhrio {:method :post
                 :uri "/upload/code"
                 :timeout 10000
                 :body val
                 ;;:format (ajax/json-request-format)
                 :response-format (ajax/json-request-format {:keywords? true})
                 :on-succss [:successful-code-req]
                 :on-failure [:failed-code-req]}}))





;;-----------------------------------------------------------
;; Domino 2: comupte effect of receiving a successful
;; :http-xhrio for :submit-code-txt
;;
;;-----------------------------------------------------------
(rf/reg-event-fx
 :successful-code-req
 (fn [{:keys [db]} [_ result]]
   (println "::-->  :successful-code-req: " result)

   (let [m  (-> db
                (assoc :show-progress-bar false)
                (assoc :code-text-results result))] 
     {:db m})))



;;-----------------------------------------------------------
;; Domino 2: comupte effect of receiving a failed
;; :http-xhrio for :submit-code-txt
;;
;;-----------------------------------------------------------
(rf/reg-event-fx
 :failed-code-req
 (fn [{:keys [db]} [_ result]]
   (println "::-->  :failed-code-req: " result)   
   (let [m  (-> db
                (assoc :show-progress-bar false)
                (assoc :code-text-error result))] 
     {:db m})))

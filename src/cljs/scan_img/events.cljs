(ns scan-img.events
  (:require
   [clojure.string :as str]
   [scan-img.db :as db]
   [scan-img.ui-stm :as ui-stm]
   [ajax.core :as ajax]
   [ajax.edn :as ajxedn]
   [re-frame.core :as rf]
   [scan-img.utils :as utils]
   [scan-img.ui-stm :as ui-stm]
   [day8.re-frame.http-fx])) ;; wil cause :http-xhrio to reister with re-frame


;;-----------------------------------------------------------
;; Initialises app-db. Event send from core.cljs
;;-----------------------------------------------------------
(rf/reg-event-db
 ::initialize-db
 (fn [db _]
   (-> db
       (merge db/default-db)
       (assoc :state (ui-stm/next-state nil :init)))))



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
         ntick (if (< tick 100) (+ tick 5) 0)
         new-tick  (if (= :SUCCESS (:state db)) 100 ntick)]
     {:db (assoc db :progress-tick ntick)})))



;;-----------------------------------------------------------
;; Domino 2: comupte effect of resetting the ticker
;;-----------------------------------------------------------
(rf/reg-event-fx
 :reset-ticker 
 (fn [{:keys [db]} [_ _]]
   {:db (assoc db :progress-tick 0)}))



;;-----------------------------------------------------------
;; Domino 2: comupte effect of resetting the form
;;-----------------------------------------------------------
(rf/reg-event-fx
 :reset-form 
 (fn [{:keys [db]} [_ _]]
   {:db  (-> db 
             (dissoc :submission-results)
             (dissoc :file-selected "")          
             (assoc  :progress-tick 0)
             (assoc :code-text "")
             (assoc :user-name "")
             (assoc :password ""))}))


;;-----------------------------------------------------------
;; Domino 2: comupte effect of selecting the upload type 
;;-----------------------------------------------------------
(rf/reg-event-fx
 :upload-type 
 (fn [{:keys [db]} [_ val]] 
   {:db (assoc db :upload-type val)}))



;;-----------------------------------------------------------
;; Update current state using state machine
;;-----------------------------------------------------------
(defn transition-state
  "Based on the currrent state and event in the app-db transition
  next state"
  [db event]
  (println "::--> Transition sate from " (:state db) " with event " event)
  (if-let [nxt-state (ui-stm/next-state (:state db) event)] 
    (assoc db :state nxt-state)
    db))




;;-----------------------------------------------------------
;; Domino 2: comupte effect of changing the code text 
;;-----------------------------------------------------------
(rf/reg-event-fx
 :modify-code 
 (fn [{:keys [db]} [evt val]]
   {:db (-> db
            (assoc :code-text val)
            (transition-state evt))
    :dispatch [:reset-ticker]}))


;-----------------------------------------------------------
;; Domino 2: comupte effect of entering or changing the user name
;;-----------------------------------------------------------
(rf/reg-event-fx
 :modify-name 
 (fn [{:keys [db]} [evt val]]
   {:db (-> db
            (assoc :user-name val)
            (transition-state evt))
    :dispatch [:reset-ticker]}))


;;-----------------------------------------------------------
;; Domino 2: comupte effect of entering or changing the
;;-----------------------------------------------------------
(rf/reg-event-fx
 :modify-password 
 (fn [{:keys [db]} [evt val]]
   {:db (-> db
            (assoc :password val)
            (transition-state evt))
    :dispatch [:reset-ticker]}))




;;-----------------------------------------------------------
;; Domino 2: comupte status of clicking submit
;; Checks and dispatches checks an event depending on information
;; 
;;-----------------------------------------------------------
(rf/reg-event-fx
 :submit-clicked
 (fn [{:keys [db]} [_ _]]
   (let [{:keys [code-text user-name password]} db
         evt (cond (str/blank? code-text)
                   [:submit-no-code]
                   (str/blank? user-name)
                   [:submit-no-name]
                   (str/blank? password)
                   [:submit-no-password]
                   :else
                   [:try-submit])]
     {:db db
      :dispatch evt})))



;;-----------------------------------------------------------
;; Domino 2: regsiter event handler for submitting with no code
;;-----------------------------------------------------------
(rf/reg-event-db
 :submit-no-code
 (fn [db [event _]]
   (transition-state db event)))

;;-----------------------------------------------------------
;; Domino 2: regsiter event handler for submitting with no user name
;;-----------------------------------------------------------
(rf/reg-event-db
 :submit-no-name
 (fn [db [event _]]
   (transition-state db event)))


;;-----------------------------------------------------------
;; Domino 2: regsiter event handler for submitting with no password
;;-----------------------------------------------------------
(rf/reg-event-db
 :submit-no-password
 (fn [db [event _]]
   (transition-state db event)))




;;-----------------------------------------------------------
;; Domino 2: comupte effect of submitting using :http-xhrio
;; you MUST provide a :response-format, it is not inferred for you.
;;-----------------------------------------------------------
(rf/reg-event-fx
 :try-submit 
 (fn [{:keys [db]} [event _]]
   (println "::--> try-submit: " db)
   (let [{:keys [code-text user-name password]} db
         payload {:code code-text :name user-name :password password}] 
     {:db (transition-state db event)
      :http-xhrio {:method :post
                   :uri "/upload/code"
                   :timeout 10000
                   :body payload
                   ;;:format (ajxedn/edn-request-format)
                   :response-format (ajxedn/edn-response-format)
                   :on-success [:handle-success]
                   :on-failure [:handle-error]}})))




;;-----------------------------------------------------------
;; Domino 2: comupte effect of receiving a successful
;; :http-xhrio for :successful-code-req
;;
;;-----------------------------------------------------------
(rf/reg-event-fx
 :handle-success
 (fn [{:keys [db]} [evt res]]
   (println "::--> handle-success: " res)

   (let [m  (-> db
                (transition-state evt)
                (assoc :show-progress-bar false)
                (assoc :submission-results res))]
     {:db m
      :dispatch [:upload-status "Commands successfuly executed"]})))



;;-----------------------------------------------------------
;; Domino 2: comupte effect of receiving a failed
;; :http-xhrio for :failed-code-req
;;
;;-----------------------------------------------------------
(rf/reg-event-fx
 :handle-error
 (fn [{:keys [db]} [evt res]]
   (println "::-->  handle-error: " res)
   
   (let [m  (-> db
                (transition-state evt)
                (assoc :show-progress-bar false)
                (assoc :submission-results res))
         msg (utils/status (str "Submit failed with error message -> " (:last-error res) " <-")
                           [(str "Status: " (:status res))
                            (str "Status text: " (:status-text res))
                            (str "Debug message: " (:debug-message res))] nil)]

     {:db m
      :dispatch [:upload-status msg]})))

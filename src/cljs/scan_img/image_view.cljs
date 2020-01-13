(ns scan-img.image-view
  (:require [cljs.reader :as cr]
            [re-frame.core :as rf]
            [scan-img.navbar :as nv]
            [scan-img.subs :as subs]
            [scan-img.utils :as utils]
            [scan-img.message-panel :as msg]
            [clojure.string :as st]
            [ajax.core :refer [POST]]))


;;(defonce selected-upload-type (atom {:upload-type "image"}))

;;-----------------------------------------------------------
;; created upload message
;;-----------------------------------------------------------
(defn upload-messages
  "Fomart message"
  [rsp]
  [(str "Message: " (:message rsp))
   (str "Filename: " (:filename rsp))
   (str "Size: " (:size rsp))
   (str "Location: " (:path rsp))])



;;--------------------------------------------------------------
;; creates an input field based on information provided for name
;; type, etc
;;--------------------------------------------------------------
(defn input-field
  "Ceates and input field component"
  [id nam typ lbl val on-chg req]
  [:div {:class "form-group col"}
   [:label {:for id} lbl]   
   [:input {:id id
            :type typ
            :class (if-not (st/blank? @val) "form-control is-valid" "form-control is-invalid")
            :value @val
            :on-change on-chg
            :required req}]])

;;-----------------------------------------------------------
;; Handle ok response
;;-----------------------------------------------------------
(defn handle-response-ok
  "Handle a successful response. The parameter will contain
  data supplie to the response object by the server"
  [resp]

  (println "::==> handle-reponse-ok: rsp: " resp)
  
  (let [rsp (cr/read-string resp)
        upl-messages (upload-messages rsp)
        cmd-messages (:cmd-results rsp)]
    ;; cmd-messages is a map with a list mapped to the key
    ;; {:results [{:command "...."
    ;;             :message "..."
    ;;             :outstrlst "..."}]}
    ;;
    (println "::==> handle-response-ok: upload nessage " upl-messages)
    (println "::==> handle-response-ok: cmd message" cmd-messages)
    (println "::==> handle-response-ok: results: " (:results cmd-messages))
    (rf/dispatch [:progress-bar/tick 100])
    (println "::==> handle-response-ok: *100* DISPATCHED!")
    (rf/dispatch [:upload-status cmd-messages])
    (rf/dispatch [:progress-bar/stop])
    (println "::==> handle-response-ok: *STOP* DISPATCHED!")))

;;-----------------------------------------------------------
;; Handle error in messahe
;;----------------------------------------------------------- 
(defn handle-response-error
  "Handle a failed uplod"
  [ctx]
  (let [rsp (js->clj (:response ctx) :keywordize-keys true)
        sts (utils/status "Upload failed!"
                          [(str "HTTP status code: "  (:status ctx))
                           (str "HTTP tatus message: " (:status-text ctx))
                           (str "Failure type: " (:failure ctx)) 
                           (str "Response message: " (:message rsp))]
                    nil)]
    (println "::-> handle-reponse-error: response " (:response ctx))
    (println "::-> handle-reponse-error: rsp " rsp)
    (rf/dispatch [:progress-bar/stop])
    (rf/dispatch [:upload-status sts] )))


;;-----------------------------------------------------------
;; Handler for upload-button
;;-----------------------------------------------------------
(defn upload-file [file-id file-type]
  ;;  (rf/dispatch [:submit-file-clicked])
  (rf/dispatch [:progress-bar/start])
  (let [file-el (.getElementById js/document file-id)
        file-name (.-name file-el)
        file-data (aget (.-files file-el) 0)
        user-name (.-value (.getElementById js/document "user-name"))
        password  (.-value (.getElementById js/document "password"))
        
        form-data (doto
                    (js/FormData.)
                    (.append file-name file-data)
                    (.append "upload-type" file-type)
                    (.append :user-name user-name)
                    (.append :password password))

        
        sts (utils/status-message (str  "Uploading file '" file-name "'") "Upload started..." nil)]
    
    
    (println "::--> image-view/upload-file: sts = " sts )
    (println "::--> image-view/upload-file: password = " password)

    (when (some? file-data)
      (POST "/upload/scan" {:body form-data
                            ;; :response-format :json
                            ;; :keywords? true
                            :handler handle-response-ok
                            :error-handler handle-response-error})

      (rf/dispatch [:upload-status sts])
      
      #_(swap! tick-status assoc :tick true)
      )))


(defn reset-form
  []
  (rf/dispatch [:reset-form]))


;;-----------------------------------------------------------
;; Upload form component
;;-----------------------------------------------------------
(defn upload-form
  []
  (let [sel-file (rf/subscribe [:file-selected])
        val-unm  (rf/subscribe [:user-name])
        val-pwd  (rf/subscribe [:password])
        inp-cplt? (or (st/blank? @sel-file)
                      (st/blank? @val-unm)
                      (st/blank? @val-pwd))
        on-chg-unm #(rf/dispatch [:modify-name (-> % .-target .-value)])
        on-chg-pwd #(rf/dispatch [:modify-password (-> % .-target .-value)])]
    
    (fn []
      [:div
       [:form {:id "upload-form"
               :enc-type "multipart/form-data"
               :method "POST"}
        [:div {:class "form-group"}
         [:div {:class "custom-file"}
          ;;[:label {:for "file"} "Docker image"]
          [:input {:type "file"
                   :class (if-not (st/blank? sel-file) "custom-file-input is-valid" "custom-file-input is-invalid")
                   :required "true"
                   :name "file"
                   :id "file"
                   :on-click #(rf/dispatch [:reset-ticker 0])
                   :on-change #(rf/dispatch [:file-selected (-> %
                                                                .-target
                                                                .-files
                                                                (aget 0)
                                                                .-name)])}]         
          [:label {:class "custom-file-label"
                   :for  "file"} @(rf/subscribe [:file-selected])]]]
        

        [:br]
        [:div {:class "row"}
         [input-field "user-name" :user-name nil         "User Name" val-unm on-chg-unm true]
         [input-field "password"  :password  "password"  "Password"  val-pwd on-chg-pwd true]] 

        [:hr]
        [:div {:class "form-group"}
         [:button {:type "reset"
                   :class "btn btn-danger float-left"
                   :on-click #(reset-form)} "Reset"]
         
         [:button {:type "button"
                   :class "btn btn-primary float-right"
                   :disabled (or (st/blank? @sel-file)(st/blank? @val-unm)(st/blank? @val-pwd)) 
                   :on-click #(upload-file "file" "image")} "Start..."]]]])))



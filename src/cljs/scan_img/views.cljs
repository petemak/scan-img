(ns scan-img.views
  (:require
   [re-frame.core :as rf]
   [scan-img.subs :as subs]))


(defn name-field
  []
  [:div
   "Title: "
   [:input {:type "text"
            :value @(rf/subscribe [::subs/name])
            :on-change #(rf/dispatch [:name-change (-> %
                                                      .-target
                                                      .-value)])}]])
 
;;-----------------------------------------------------------
;; Upload indicator
;;-----------------------------------------------------------
(defn status-indicator []
  [:div 
   [:p "Uploading file... "
    [:span {:class "fa fa-spinner fa-spin fa-pulse"}]]])




;;-----------------------------------------------------------
;; Ajax functions
;;-----------------------------------------------------------
(defn handle-response-ok [resp]
  (let [rsp (js->clj resp :keywordize-keys true)
        status (set-status "alert alert-success"
                           "Upload Successful"
                           [(str "Filename: " (:filename rsp))
                            (str "Size: " (:size rsp))
                            (str "Tempfile: " (:tempfile rsp))])]
    (rf/dispatch [:upload-status status])))

(defn handle-response-error [ctx]
  (let [rsp (js->clj (:response ctx) :keywordize-keys true)
        status (set-status "alert alert-danger"
                           "Upload Failure"
                           [(str "Status: " (:status ctx) " "
                                 (:status-text ctx))
                            (str (:message rsp))])]
    (.log js/console (str "Upload error: " status))
    (rf/dispatch [:upload-status status] )))

(defn upload-file [element-id]
  (let [el (.getElementById js/document element-id)
        name (.-name el)
        file (aget (.-files el) 0)
        form-data (doto (js/FormData.)
                    (.append name file))]
    (POST "/upload" {:params form-data
                     :response-format :json
                     :keywords? true
                     :handler handle-response-ok
                     :error-handler handle-response-error})
    (set-upload-indicator)))

(defn upload-button []
  [:div
   [:hr]
   [:button {:class "btn btn-primary"
             :type "button"
             :on-click #(upload-file "upload-file")}
    "Upload" [:span {:class "fa fa-upload"}]]])





;;-----------------------------------------------------------
;; Upload component
;;-----------------------------------------------------------
(defn upload-form []
  [:div
   [:form {:id "upload-form"
           :enc-type "multipart/form-data"
           :method "POST"}
    [:label "Filename: "]
    [:input {:type "file"
             :name "upload-file"
             :id "upload-file"}]]])


(defn main-panel
  []
  (let [name (rf/subscribe [::subs/name])]
    [:div
     [:h1 "Remote Task Executer: " @name]
     [:hr]
     
     [upload-form]
     [upload-button]]))

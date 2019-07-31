(ns scan-img.views
  (:require [re-frame.core :as rf]
            [scan-img.subs :as subs]
            [ajax.core :refer [POST]]))


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
;; Upload status indicator
;;-----------------------------------------------------------
(defn status [title messages]
  {:title title
   :messages messages})


;;-----------------------------------------------------------
;; Indicator
;;----------------------------------------------------------
(defn status-indicator []
  (let [status @(rf/subscribe [:upload-status])]
    [:div
     [:div {:class "progress"}
      [:div {:class ["progress-bar" "progress-bar-striped" "bg-success"]
             :role "progressbar"
             :style {:width "25%"}
             :aria-valuenow "25"
             :aria-valuemin "0"
             :aria-valuemax "100"} "25%"]]
     [:br]
     [:div {:class "alert alert-success" :role "alert"}
      [:h4 (:title status)]
      [:ul
       (for [msg (:messages status)]
         [:li msg])]]]))

;;-----------------------------------------------------------
;; Ajax functions
;;-----------------------------------------------------------
(defn handle-response-ok [resp]
  (let [rsp (js->clj resp :keywordize-keys true)
        sts (status "Upload succeeded"
                    [(str "Filename: " (:filename rsp))
                     (str "Size: " (:size rsp))
                     (str "Tempfile: " (:tempfile rsp))])]
    (rf/dispatch [:upload-status sts])))

(defn handle-response-error [ctx]
  (let [rsp (js->clj (:response ctx) :keywordize-keys true)
        sts (status "Upload failed!"
                    [(str "HTTP status code: "  (:status ctx))
                     (str "HTTP tatus message: " (:status-text ctx))
                     (str "Failure type: " (:failure ctx))
                     (str "Response message: " (:message rsp))])]
    (.log js/console (str "Upload error: " sts))
    (rf/dispatch [:upload-status sts] )))


;;-----------------------------------------------------------
;; Handler for upload-button
;;-----------------------------------------------------------
(defn upload-file [element-id]
  (let [el (.getElementById js/document element-id)
        name (.-name el)
        file (aget (.-files el) 0)
        form-data (doto
                      (js/FormData.)
                      (.append name file))
        sts (status "Uploading file..." [])]
    (POST "/upload" {:body form-data
                     :response-format :json
                     :keywords? true
                     :handler handle-response-ok
                     :error-handler handle-response-error})

    (rf/dispatch [:upload-status sts])))


;;-----------------------------------------------------------
;;  component
;;-----------------------------------------------------------
(defn upload-button []
   [:button {:class "btn btn-primary"
            :type "button"
            :on-click #(upload-file "file-upload-input")}
    "Upload ..." ] )





;;-----------------------------------------------------------
;; Upload form component
;;-----------------------------------------------------------
(defn upload-form []
  [:div
   [:form {:id "upload-form"
           :enc-type "multipart/form-data"
           :method "POST"}
    [:div {:class "form-group"}
     [:div {:class "custom-file"}
      [:input {:type "file"
               :class "custom-file-input"
               :required "true"
               :name "file-upload-input"
               :id "id-file-upload-input"
               :on-change #(rf/dispatch [:file-selected (-> %
                                                            .-target
                                                            ;;.-value
                                                            .-files
                                                            (aget 0)
                                                            .-name)])}]
      [:label {:class "custom-file-label"
               :for  "file-upload-input"} @(rf/subscribe [:file-selected])]]]
    [:div {:class "form-group"}
     [:button {:type "reset"
               :class "btn btn-danger float-left"} "Reset"]  
     [:button {:type "button"
               :class "btn btn-primary float-right"
               :on-click #(upload-file "id-file-upload-input")} "Upload & Scan..."]]]])




;;----------------------------------------------------------
;; Main view and entry
;;----------------------------------------------------------
(defn main-panel
  []
  (let [name (rf/subscribe [::subs/name])]
    [:div.container
     [:div.container
      [:nav.navbar.navbar-dark.bg-primary
       [:span.navbar-text "Remote Task Executer: " @name]] ]
     [:br]
     [:div.container.conatiner_fluid
      [:div.row
       [:div.col [upload-form]]]      
      [:hr]
      [:div.row
       [:div.col [status-indicator]]]]]))

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
(defn status [class title messages]
  [:div {:class class}
   [:h4 title]
   (into [:ul]
         (for [msg messages]
           [:li msg]))])


;;-----------------------------------------------------------
;; Indicator
;;----------------------------------------------------------
(defn status-indicator []
  [:div {:class "progress"}
   [:div {:class ["progress-bar" "progress-bar-striped" "bg-success"]
          :role "progressbar"
          :style {:width "25%"}
          :aria-valuenow "25"
          :aria-valuemin "0"
          :aria-valuemax "100"} "25%"]
   @(rf/subscribe [:upload-status])])

;;-----------------------------------------------------------
;; Ajax functions
;;-----------------------------------------------------------
(defn handle-response-ok [resp]
  (let [rsp (js->clj resp :keywordize-keys true)
        sts (status "alert alert-success"
                           "Upload succeeded"
                           [(str "Filename: " (:filename rsp))
                            (str "Size: " (:size rsp))
                            (str "Tempfile: " (:tempfile rsp))])]
    (rf/dispatch [:upload-status sts])))

(defn handle-response-error [ctx]
  (let [rsp (js->clj (:response ctx) :keywordize-keys true)
        sts (status "alert alert-danger"
                    "Upload failed"
                    [(str "Status: "  (:status ctx) " " (:status-text ctx))
                     (str "Message: " (:message rsp))])]
    (.log js/console (str "Upload error: " sts))
    (rf/dispatch [:upload-status sts] )))

(defn upload-file [element-id]
  (let [el (.getElementById js/document element-id)
        name (.-name el)
        file (aget (.-files el) 0)
        form-data (doto (js/FormData.)
                    (.append name file))
        sts (status "fa fa-spinner fa-spin fa-pulse"
                    "Uploading file..."
                    [])]
    (POST "/upload" {:params form-data
                     :response-format :json
                     :keywords? true
                     :handler handle-response-ok
                     :error-handler handle-response-error})

    (rf/dispatch [:upload-status sts])))


;;-----------------------------------------------------------
;;  component
;;-----------------------------------------------------------
(defn upload-button []
  [:div
   [:button {:class "btn btn-primary"
             :type "button"
             :on-click #(upload-file "file-upload-input")}
    "Upload ..." ]])





;;-----------------------------------------------------------
;; Upload form component
;;-----------------------------------------------------------
(defn upload-form []
  [:div
   [:form {:id "upload-form"
           :enc-type "multipart/form-data"
           :method "POST"}
    [:div {:class "custom-file"}
     [:input {:type "file"
              :class "custom-file-input"
              :name "file-upload-input"
              :id "file-upload-input"
              :on-change #(rf/dispatch [:file-selected (-> %
                                                           .-target
                                                           .-value)])}]
     [:label {:class "custom-file-label"
              :for "file-upload-input"} @(rf/subscribe [:file-selected])]]]])






;;----------------------------------------------------------
;; Main view and entry
;;----------------------------------------------------------
(defn main-panel
  []
  (let [name (rf/subscribe [::subs/name])]
    [:div
     [:h1 "Remote Task Executer: " @name]
     [:hr]
     [:div.container
      [:div.row
       [:div.col [upload-form]]]
      
      [:div.row
       [:div.col [status-indicator]]]

      [:div.d-flex.flex-row-reverse
       [:div.p-2  [upload-button] ]]]]))

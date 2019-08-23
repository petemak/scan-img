(ns scan-img.views
  (:require [cljs.reader :as cr]
            [re-frame.core :as rf]
            [scan-img.subs :as subs]
            [ajax.core :refer [POST]]))



(defonce tick-status (atom {:tick false}))

;;----------------------------------------------------------
;; Ticker event to drive progress monitor
;;----------------------------------------------------------
(defn dispatch-tick
  "Dispatch ticking event"
  []
  (if (:tick (deref tick-status))
    (rf/dispatch [:progress-tick])))


;; call the ticker dispatching function every half a second
(defonce ticker (js/setInterval dispatch-tick 400))

 
;;-----------------------------------------------------------
;; Upload status indicator
;;-----------------------------------------------------------
(defn status [title
              upl-messages
              cmd-messages]
  {:title title
   :upl-messages upl-messages
   :cmd-messages cmd-messages})


;;-----------------------------------------------------------
;; Indicator
;;----------------------------------------------------------
(defn status-indicator []
  (let [status @(rf/subscribe [:upload-status])
        tick @(rf/subscribe [:progress-tick])
        ptick (str tick "%")]
    (println "::--> Status --[" status "]--")
    (println "::--> Tick --[" tick "]--")
    
    (when (= "100" tick)
      (println "::--> Sopping tick --[" tick "]--")       
      (swap! tick-status assoc :tick false))
    [:div
     [:div {:class "progress"}
      [:div {:class ["progress-bar" "progress-bar-striped" "bg-success"]
             :role "progressbar"
             :style {:width ptick}
             :aria-valuenow tick
             :aria-valuemin "0"
             :aria-valuemax "100"} ptick]]
     [:br]
     [:div {:class "alert alert-success" :role "alert"}
      [:h4 (:title status)]
      [:ul
       (for [msg (:upl-messages status)]
         [:li {:key (subs msg 0 2)} msg])]

      (if (some? (:cmd-messages status))
        [:h4 "Scan results"])

      (let [cmd-messages (:cmd-messages status)
            output-msg (:message cmd-messages)
            output-list (:outstrlst cmd-messages)]
           
      
        (if (some? cmd-messages)
          [:ul
           [:li {:key "msg"}  (str ":message " (:message cmd-messages))]
           [:li {:key "exit"}  (str ":exit " (:exit cmd-messages))]
           [:li {:key "err"} (str ":err " (:err cmd-messages))]
           (for [out output-list]
             [:li {:key (subs out 0 2)} out])]))]]))

;;-----------------------------------------------------------
;; Ajax halder functions
;;-----------------------------------------------------------
(defn upload-messages
  [rsp]
  [(str "Message: " (:message rsp))
   (str "Filename: " (:filename rsp))
   (str "Size: " (:size rsp))
   (str "Location: " (:path rsp))])

(defn handle-response-ok
  "Handle a successful response. The parameter will contain
  data supplie to the response object by the server"
  [resp]

  (println "::==> handle-reponse-ok: rsp: " resp)
  
  (let [rsp (cr/read-string resp)
        upl-messages (upload-messages rsp)
        cmd-messages (:cmd-results rsp)   
        sts (status "Upload succeeded"
                    upl-messages
                    cmd-messages)]
    (println "::==> handle-reponse-ok: upload nessage " upl-messages)
    (println ":==> handle-reponse-ok: cmd message" cmd-messages)
    (rf/dispatch [:reset-ticker 100])
    (rf/dispatch [:upload-status sts])))

(defn handle-response-error
  "Handle a failed uplod"
  [ctx]
  (let [rsp (js->clj (:response ctx) :keywordize-keys true)
        sts (status "Upload failed!"
                    [(str "HTTP status code: "  (:status ctx))
                     (str "HTTP tatus message: " (:status-text ctx))
                     (str "Failure type: " (:failure ctx))
                     (str "Response message: " (:message rsp))]
                    nil)]
    (println "::-> handle-reponse-error: rsp " rsp)
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
        sts (status (str  "Uploading file '" name "'") [] nil)]
    (when (some? file)
      (POST "/upload/scan" {:body form-data
                            ;; :response-format :json
                            ;; :keywords? true
                            :handler handle-response-ok
                            :error-handler handle-response-error})

      (rf/dispatch [:upload-status sts])
      (swap! tick-status assoc :tick true))))


(defn reset-form
  []
  (rf/dispatch [:reset-form]))


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
               :name "file"
               :id "file"
               :on-click #(rf/dispatch [:reset-ticker 0])
               :on-change #(rf/dispatch [:file-selected (-> %
                                                            .-target
                                                            ;;.-value
                                                            .-files
                                                            (aget 0)
                                                            .-name)])}]
      [:label {:class "custom-file-label"
               :for  "file"} @(rf/subscribe [:file-selected])]]]
    [:div {:class "form-group"}
     [:button {:type "reset"
               :class "btn btn-danger float-left"
               :on-click #(reset-form)} "Reset"]
     
     [:button {:type "button"
               :class "btn btn-primary float-right"
               :on-click #(upload-file "file")} "Scan image..."]]]])




;;----------------------------------------------------------
;; Main view and entry
;;----------------------------------------------------------
(defn main-panel
  []
  [:div.container
   [:div.container
    [:nav.navbar.navbar-dark.bg-primary
     [:span.navbar-text "Docker Image Scanner"]] ]
   [:br]
   [:div.container.conatiner_fluid
    [:div.row
     [:div.col [upload-form]]]      
    [:hr]
    [:div.row
     [:div.col [status-indicator]]]]])

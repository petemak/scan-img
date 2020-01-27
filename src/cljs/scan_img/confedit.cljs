(ns scan-img.confedit
  (:require [cljs.reader :as cr]
            [re-frame.core :as rf]
            [paren-soup.core :as ps]            
            [scan-img.utils :as utils]
            [ajax.core :refer [POST]]))


(defn reset-form
  "Event handler for resetting content"
  []
  (rf/dispatch [:progress-bar/reset]))


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


;;-----------------------------------------------------------
;; Handle ok response
;;-----------------------------------------------------------
(defn handle-response-ok
  "Handle a successful response. The parameter will contain
  data supplie to the response object by the server"
  [resp]

  (println "::==> confedit/handle-reponse-ok: rsp: " resp)
  
  (let [results (cr/read-string resp)]
    ;; cmd-messages is a map with a list mapped to the key
    ;; {:results [{:command "...."
    ;;             :message "..."
    ;;             :outstrlst "..."}]}
    (println "::==> confedit/handle-response-ok: results: " results)
    (rf/dispatch [:progress-bar/tick 100])
    (rf/dispatch [:upload-status results])
    (rf/dispatch [:progress-bar/stop])))

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
    (println "::==> confedit/handle-reponse-error: response " (:response ctx))
    (println "::==> confedit/handle-reponse-error: rsp " rsp)
    (rf/dispatch [:progress-bar/stop])
    (rf/dispatch [:upload-status sts] )))


;;-----------------------------------------------------------
;; Handler for upload-button
;;
;; POSTs to the selected image to the /upload/scan URL 
;;-----------------------------------------------------------
(defn save-config-clicked [element-id file-type]
  ;;  (rf/dispatch [:submit-file-clicked])
  (rf/dispatch [:progress-bar/start])
  (let [config-text (.-textContent (.getElementById js/document element-id))
        form-data (doto
                    (js/FormData.)
                    (.append "config" config-text)
                    (.append "upload-type" file-type))
        sts (utils/status-message (str  "Saving configuration  '" config-text "'") "Please wait!" nil)]
    
  
    (when (some? config-text)
      (POST "/upload/config" {:body form-data
                              ;; :response-format :json
                              ;; :keywords? true
                              :handler handle-response-ok
                              :error-handler handle-response-error})

      (rf/dispatch [:upload-status sts]))))


;;-----------------------------------------------
;; Initialise paren-soup
;;-----------------------------------------------
(defn editor
  []
  (fn []
    (let [config-text @(rf/subscribe [:config-view/config])] 
      [:div
       [:div {:class "paren-soup" :id "paren-soup"}
       ;; [:div {:class "instarepl" :id "instarepl"}]
        [:div {:class "numbers" :id "numbers"}]
        [:div {:class "content" :id "content" :contenteditable "true" }
         ;; Add code here
         ;;
         (if (nil? config-text) ";; Add code here!" (-> config-text
                                                        (:results)
                                                        (first)
                                                        (:config)))]
        (ps/init-all)]
       

       [:hr]
       [:div {:class "form-group"}
        [:button {:type "reset"
                  :class "btn btn-danger float-left"
                  :on-click #(reset-form)} [:i {:class "far fa-trash-alt"}] " Clear"]
        
        [:button {:type :submit
                  :class "btn btn-primary float-right"
                  :on-click #(save-config-clicked "content" "config-file")} [:i {:class "far fa-save"}] " Save"]]])))

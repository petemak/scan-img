(ns scan-img.utils)


  
;;-----------------------------------------------------------
;; Upload status indicator
;;-----------------------------------------------------------
(defn status [title
              upl-messages
              cmd-messages]
  {:title title
   :upl-messages upl-messages
   :cmd-messages cmd-messages})


(defn status-message
  [cmd msg det]
  {:results [{:command cmd
              :message msg
              :outstrlst det}]})

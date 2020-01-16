(ns scan-img.utils
  (:require [clojure.string :as cs]))


  
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


(defn not-blank?
  "Verifies if string is not null and not balnk"
  [s]
  (not (cs/blank? s)))


(defn unique-key
  "Generate a unique identifier for lists"
  [s]
  (if (cs/blank? s)
    (str "id-" (rand-int 1000))    
    (str "id-" s (rand-int 1000))))

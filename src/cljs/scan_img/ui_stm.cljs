(ns scan-img.ui-stm)


;; State diagram
;;
;;        state         |     Submit button      |     Success Message     |     Error message       |
;;----------------------------------------------------------------------------------------------------
;;  "READY"                Enabled                   ""                       ""
;;  "CODE-REQUIRED"        Disabled                  ""                       "Docker file required"     
;;  "CODE-CHANGED"         Enabled                   ""                       ""     
;;  "USERNAME-REQUIRED"    Disabled                  ""                       "User name required"
;;  "USERNAME-CHANGED"     Enabled                   ""                       ""     
;;  "PASSWORD-REQUIRED"    Disabled                  ""                       "User name required"
;;  "PASSWORD-CHANGED"     Enabled                   ""                       ""
;;  "ERROR-RESPONSE"       Enabled                   ""                       "Correct input and try again"
;;  "SUCCESS"              Enabled                   ""                       "Docker commands successfuly executed"
 

(def stm {nil                   {:init               :READY}
          :READY                {:submit-no-code     :CODE-REQUIRED
                                 :submit-no-name     :USERNAME-REQUIRED
                                 :submit-no-password :PASSWORD-REQUIRED
                                 :try-submit         :SUBMITTING-DATA}
          :SUBMITTING-DATA      {:handle-error       :ERROR-RESPONSE
                                 :handle-success     :SUCCESS}
          :SUCCESS              {:modify-code        :READY}
          :CODE-REQUIRED        {:modify-code        :READY}
          :USERNAME-REQUIRED    {:modify-name        :READY}
          :PASSWORD-REQUIRED    {:modify-password    :READY}          
          :ERROR-RESPONSE       {:modify-code        :READY
                                 :modify-name        :READY
                                 :modify-password    :READY}})


(defn next-state
  "Returns next state for the FSM given the
  current state and event"
  [current-state event]
  (get-in stm [current-state event]))


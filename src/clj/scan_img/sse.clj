(ns scan-img.sse
  (:require [ring.sse :as rsse]
            [clojure.core.async :as async]))


(defn sse-handler
  [in-ch]
  (rsse/event-channel-handler
   (fn [request response raise out-ch]
     (async/go-loop [data (async/<! in-ch)]
       (when data
         (async/>! out-ch )
         (recur (async/<! in-ch)))))))



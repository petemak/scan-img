(ns scan-img.async-cmd-exec
  (:require [clojure.core.async :as async]))


;; Publisher channel
(def publishing-chan (async/chan 1))

;; Create a publication on the publisher channel
(def publication (async/pub publishing-chan :topic))

;; logging channel
(def logging-chan (async/chan 1))


;;-----------------------------------------------
;; Start the logging channel
;;-----------------------------------------------
(defn start-log-chan
  "Start logging channel and process incomming
  messages"
  [lc]
  (async/go-loop []
    (when-let [message (async/<! lc)]
      (println "::-> log chanel: " message)
      (recur))))


;;-----------------------------------------------
;; close all channels
;;-----------------------------------------------
(defn close-channels!
  []
  (async/close! logging-chan)
  (async/close! publishing-chan))




;;-----------------------------------------------
;; Subscribe a channel to a topic of a publication
;;-----------------------------------------------

(defn subscribe
  "Subscribes a channel to a topic of a publication.
  "
  [publication subscriber topics callback]
  (let [subs-chan (async/chan 1)]
    (doseq [topic topics]
      ;; Subsribe a channel to a publicatio about a topic
      (async/sub publication topic subs-chan))
    
    (async/go-loop []
      (when-let [message (async/<! subs-chan)]
        ;; When something comes in on the channel then callback the
        ;; subsriber
        (async/>! logging-chan (callback subscriber topics message))
        (recur)))))


;; Simple callback generates a string
(defn callback
  [subscriber topic message]
  (pr-str 
    (format "%s received message: %s"
            subscriber message)))



(defn send-message
  [channel message]
  (doseq [topic (:topics message)]
    (println "Sending topic ..." topic)
    (async/>!! channel {:topic topic
                        :message message})))


(defn read-all!!
  "Loops reading from channel ch until its closed. Returns
  content in a collection"
  [ch]
  (loop [acc []]
    (if-let [x (async/<!! ch)]
      (recur (conj acc x))
      acc)))


(defn put-all!!
  "Takes a channel and a seq and writes all
  contents of the seq to the channel"
  [ch col]
  (doseq [x col]
    (async/>!! ch x))
  (async/close! ch))


(defn go-inc
  "Demo how efficient go blocks are. Implements addition by
  iterating on a function that cretes a go bloch each time.
  The go block increments x. Return nth"
  [x n]
  (async/<!! (nth (iterate #(async/go (inc (async/<! %)))
                           (async/go x))
                  n)))


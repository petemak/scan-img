(ns foundation.repl
  (:require [clojure.string :as str]
            [clojure.data.json :as json] ))


;; Demo dynamic interactio 1
;; Counting words in a text file
(defn count-words
  "Count words in a text file"
  [fp n]
  (->> (clojure.java.io/file fp)
       slurp
       clojure.string/lower-case
       (re-seq #"\w+")
       frequencies
       (sort-by val >)
       (take n)))



(defn tokenize
  [s]
  (str/split s #"[\s ? - , : ;]+"))


;;
;; Demo dynamic interactio 2
;; Counting 
(defn jokes
  []
  (->>  "https://official-joke-api.appspot.com/jokes/ten"
        slurp
        json/read-json
        (map #(select-keys % [:setup :punchline]))
        (map #(str (:setup %) "-" (:punchline %)))
        (str/join " ")
        tokenize
        frequencies
        (sort-by (comp - val))))

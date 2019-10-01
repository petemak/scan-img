(ns scan-img.utils
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [selmer.parser :as selmer]
            [taoensso.timbre :as timbre]))


;;--------------------------------------------------------------
;; Generate a uuid as string for prefixing file names
;;--------------------------------------------------------------
(defn uid-str!
  "Generates a unique identifier as a string"
  []
  (-> (.toString (java.util.UUID/randomUUID))
      (str/split #"-")
      (#(drop-last 2 %))
      (#(str/join "-" %))))


;;--------------------------------------------------------------
;; Createsunique file names
;;
;; TODO: is this the most efficient method???
;;--------------------------------------------------------------
(defn unique-str
  "modify the specified string to a unique version
  Outcome: XXXXX-XXXX-XXXX-XXXXX-s"
  [s]
  (str (uid-str!) "-" s))

;;--------------------------------------------------------------
;; Ensuring upload storage directory exists.
;; side effects!!!
;;--------------------------------------------------------------
(defn ensure-parent-dir!
  "Check and create parent directory if it doesnt exist.
  Uses canonical path to remove OS dependent path strings.
  -> seems make-parents fails gracefully returning false
  in case of error !!!!"
  [f]
  (let [can-path (.getCanonicalPath f)
        parent-file (io/as-file (.getParent f))]
    
    (if (not (.isDirectory parent-file))
      (io/make-parents can-path))
    can-path))




;;--------------------------------------------------------------
;; Slurp EDN file
;;--------------------------------------------------------------
(defn read-edn-file
  "Read an edn file from the specified path"
  [path]
  (-> path
      (slurp)
      (edn/read-string)))


;;--------------------------------------------------------------
;; For config files provided as EDN files in the resource directory
;;--------------------------------------------------------------
(defn edn-from-resource-path
  "Read an edn resource file of given name from path. This could be
  the resources/ directory or the root directory of the project."
  [name]
  (try
    (when-let [r (io/resource  name)]
         (edn/read-string (slurp r)))
    (catch Exception e
      (println "Erorr reading " name)
      (println e)
      nil)))

;;--------------------------------------------------------------
;; For config files provided as EDN files in user home
;;--------------------------------------------------------------
(defn edn-from-home
  "Read edn from a file from users home directory"
  ([file-name]
   (try
 
     (let [home-dir (System/getProperty "user.home")
           path-sep (System/getProperty "file.separator") 
           full-path (str home-dir path-sep file-name)]
       (read-edn-file full-path))
     (catch Exception e
       nil)))
  ([]
   (edn-from-home "config.edn")))

;;--------------------------------------------------------------
;; For config files provided as EDN files in memory java.io.File
;;--------------------------------------------------------------
(defn edn-from-file
  "Read an edn resource file of given name from path. This could be
  the resources/ directory or the root directory of the project."
  [file]
  (try
    (-> file
        (slurp)
        (edn/read-string))
    (catch Exception e
      (println "::-> edn-from-file - erorr reading edn from -[" file "]-")
      (println e)
      nil)))

;;--------------------------------------------------------------
;; For config files provided as EDN files in user home
;;--------------------------------------------------------------
(defn read-config
  []
  (if-let [cfg (edn-from-resource-path "config.edn")]
    cfg
    (edn-from-home)))


;;--------------------------------------------------------------
;; For config files provided as EDN files in user home
;;--------------------------------------------------------------
(defn replace-placeholder-from-map-old
  "given a string list with place-holders
  [\"command\" \"params {{xyz}}\"], replace these with values in the
  context mam {:xyz \"blab\"}"
  [ctx-map str-lst]
  (let [modified-str (selmer/render (second str-lst) ctx-map)]
    (assoc str-lst 1 modified-str)))

(defn replace-placeholder-from-map
  "given a string list with place-holders
  [\"command\" \"params {{xyz}}\"], replace these with values in the
  context mam {:xyz \"blab\"}"
  [ctx-map str-lst]
  (map #(selmer/render % ctx-map) str-lst))




;;--------------------------------------------------------------
;; Utility function for extracting results from
;; an exception
;;--------------------------------------------------------------
(defn exception->strlst
  "Given an exception object like
    {:exit -35545
     :exception #error {:cause \"error=2, No such file or directory\"
                        :via []  .... }}
    then will generate a list of strings"
  [e]
  (let [m (Throwable->map e)]
    (into [] (map str (:via m)))))

;;--------------------------------------------------------------
;; Utility function for converting maps
;; to strings
;;--------------------------------------------------------------
(defn map->strlst
  "conver a map {:a 1 :b 2 :c 3}  to a list of strings 
  [\":a 1\" \":b 2\" \":c 3\"]"
  [m]
  (into []        
     (let [ks (keys m)]
       (for [k ks]
         (str k " " (k m))))))



;;--------------------------------------------------------------
;; Utility function for extracting results from
;; an exception
;;--------------------------------------------------------------
(defn exception->strlst2
  "Given an exception object like
    {:exit -35545
     :exception #error {:cause \"error=2, No such file or directory\"
                        :via []  .... }}
    then will generate a list of strings"
  [e]
  (-> e
      (Throwable->map)
      (:via)
      (first)
      (map->strlst)
      (#(interpose "\n " %))
      (#(apply str %))))



;;--------------------------------------------------------------
;; Utility functions for extracting results from
;; a command execution
;;--------------------------------------------------------------
(defn concat-vals
  "Concatenates a value to the existing one in m witk key k"
  [m k v2]
  (let [v1 (get m k)]
    (if (some? v1)
      (assoc m k (into [] (concat v1 ["------------------------------"] v2)))
      (assoc m k v2))))


;;--------------------------------------------------------------
;; Utility functions for extracting results from
;; a command execution
;;--------------------------------------------------------------
(defn out-map
  "Create a success message object like
    {:exit 0
     :out \"\"
     :err
     :exception nil}}"
  [cmd result accum]
  (let [result (-> {}
                   (assoc :command cmd)
                   (assoc :message "Command executed successfuly")
                   (assoc :outstrlst (str/split (:out result) #"\n")))]
    (update accum :results conj result)))


;;--------------------------------------------------------------
;; Utility functions for extracting results from
;; a command execution
;;--------------------------------------------------------------
(defn err-map
  "Create an , given an err result string like
    {:exit -35545
     :err  \"Execution results..\"}}"
  [cmd result accum]
  (let [result (-> {}
                   (assoc :command cmd)
                   (assoc :message "Command executed but exited with an error.")
                   (assoc :outstrlst (str/split (:err result) #"\n")))]
    (update accum :results conj result)))


;;--------------------------------------------------------------
;; Utility functions for extracting results from
;; a command execution and adding them to the accumlator
;;--------------------------------------------------------------
(defn exc-map
  "Create an execption result, given a map with an execption object like
    {:exit -35545
     :exception #error {:cause \"error=2, No such file or directory\"
                        :via []  .... }}"
  [cmd result accum]
  (let [result (-> {}
                   (assoc :command cmd)
                   (assoc :message "Command executed but exited with an exception.")
                   (assoc :outstrlst (exception->strlst2 (:exception result))))]
    (update accum :results conj result)))





;;--------------------------------------------------------------
;; Utility functions for extracting results from
;; a command execution
;;--------------------------------------------------------------
(defn execresult->strlist
  "Converts results of the command cmd to a list os strings.
  The expcted result map looks as follows in case an
  exception happened:
  
  {:exit -559038737,
   :out nil,
   :err \"ls: illegal option...\",
   :exception #error {...}}"
  [cmd result accum]
  (timbre/info "::->  execresult->strlist - for commnd: " cmd)
  (timbre/info "::->  execresult->strlist - results for commnd: " result)
  (cond
    (nil? result) (assoc accum :outstrlst ["Unknown executaion error!"
                                           "Examine server logs e.g. figwheel_server.log"])
    (some? (:out result)) (out-map cmd result accum)
    (some? (:err result)) (err-map cmd result accum)
    (some? (:exception result)) (exc-map cmd result accum)))


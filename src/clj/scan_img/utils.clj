(ns scan-img.utils
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [selmer.parser :as selmer]))


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
  (println "::-> Path = " path)
  (-> path
      (slurp)
      (edn/read-string)))


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
(defn format-lst-map
  "given a string list with palce-holders
  {{xyz}}, replace these with values in the
  context mam {:xyz \"blab\"}"
  [str-lst ctx-map]
  (loop [slst str-lst
         ret []]
    (println "string to replace: " str)
    (println "context map: " ctx-map)
    (if (empty? slst)
      ret
      (recur (rest slst) (conj ret (selmer/render (first slst) ctx-map))))))


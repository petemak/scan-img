(ns foundation.datomic
  (:require [datomic.api :as d]
            [clojure.java.io :as io]))

;; Load schemam and data from files
(def schema (read-string (slurp (io/resource "db/schema.edn"))))
(def data (read-string  (slurp (io/resource "db/data.edn"))))

;; Next, create the URI that allows to create and connect to a database.
;;The URI for a memory storage has 3 parts:
;;
;; - "datomic", identifying it as a Datomic URI
;; - "mem", the mem storage protocol instead of a persistent store
;; - "hello", the name of the database
(def db-uri "datomic:mem://movies")


;; To interact with a datomic database
;; we need a connection to the peer. Connections are
;; created using the database URI
;;
;; conn  --> peer ---> Query endinge
;;               |
;;                ---> Transactor
;; 
(defn connect
  "Creates an in memory databse and connects the
   the peer library to the database. Returns a
   connection if successful"
  [uri]
  (if (d/create-database uri)
    (d/connect uri)))

(def conn (connect db-uri))


;; Now we can transact the schema
(defn transact-schema
  "Write schema to db"
  [conn]
  (d/transact conn schema))


(defn transact-data
  "Write test data to dab"
  [conn]
  (d/transact conn data))


(defn init-db
  "Connects to the db transacts schema and data
  and returns a map containing
  :db-created? - bolean to singify if creation succeeded
  :conn - the coonection
  :schema - result of tracting the schema
  :db     - the current databse value after transacting data
  
  Note: schema ans db are futures and contain the following if
        the transaction completes
  :db-before         database value before the transaction
  :db-after          database value after the transaction
  :tx-data           collection of Datoms produced by the transaction
  :tempids           argument to resolve-tempids"
  
  []
  (let [created? (d/create-database db-uri)
        conn   (d/connect db-uri)
        schema (d/transact conn schema)
        db     (d/transact conn data)]
    {:db-created created? :conn conn :schema schema :db db}))



(def all-movies
  '[:find ?e ?title
    :where
    [?e :movie/title ?title]])


(def movies-of-year
  '[:find ?title
    :where
    [?e :movie/year 1985]
    [?e :movie/title ?title]])

;;(d/q movies-of-year (d/db conn))


;; Parameters
(def movies-of-actor
  '[:find ?title
    :in $ ?name
    :where
    [?p :person/name ?name]
    [?m :movie/cast ?p]
    [?m :movie/title ?title]])

;;(d/q movies-of-actor db "Arnold Schwazenegger")

(def who-directed-actor
  '[:find ?director-name
    :in $ ?actor-name
    :where
    [?p :person/name ?actor-name]
    [?m :movie/cast ?p]
    [?m :movie/director ?d]
    [?d :person/name ?director-name]])

;;(d/q who-directed-actor (d/db conn)  "Arnold Schwazenegger")


;; or
(def year-by-title
  '[:find ?title ?year
    :in $ [?title ...]
    :where
    [?e :movie/title ?title]
    [?e :movie/year ?year]])

;;(d/q year-by-title db ["Lethal Weapon" "Lethal Weapon 2" "Lethal Weapon 3"])


;; Predicates
(def movies-before
  '[:find ?title
    :in $ ?date
    :where
    [?m :movie/title ?title]
    [?m :movie/year ?year]
    [(< ?year ?date)]])

;;(d/q movies-before db 1984)

(def actor-older-and-movies
  '[:find ?actor ?title
    :in $ ?name
    :where
    [?a :person/name ?name]
    [?a :person/born ?max-year]
    [?p :person/born ?year]
    [?p :person/name ?actor]
    [?m :movie/cast ?p]
    [?m :movie/title ?title]
    [(< ?year ?max-year)]])

;;(d/q actor-older-and-movies db "Danny Glover")




;; Relations
(def earnings
  [["Die Hard" 14030000]
   ["Alien" 104931801]
   ["Commando" 54791000]])

;; Find 
(def earnings-of-director
  '[:find ?title ?earnings
    :in $ ?director
    [?p :person/name ?director]
    [?m :movie/direcor ?p]
    [?m :movie/title ?title]])



(defn run-query
  [query conn]
  (d/q query (d/db conn)))


 


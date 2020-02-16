(ns scan-img.schema)

(def user-schema
  [{:db/ident :user/id
    :db/valueType :db.type/string
    :db/cardinality :db.cardinality/one
    :db/doc "The users login id"}

   {:db/ident :user/password
    :db/valueType :db.type/string
    :db/cardinality :db.cardinality/one
    :db/doc "The users password (encrypted)"}

   {:db/ident :user/name
    :db/valueType :db.type/string
    :db/cardinality :db.cardinality/one
    :db/doc "The user's full name"}
   
   {:db/ident :user/email
    :db/valueType :db.type/string
    :db/cardinality :db.cardinality/one
    :db/doc "The user's email"}
   
    {:db/ident :token/val
    :db/valueType :db.type/string
    :db/cardinality :db.cardinality/one
    :db/doc "Token string"}
   
   {:db/ident :token/iat
    :db/valueType :db.type/string
    :db/cardinality :db.cardinality/one
    :db/doc "Token string"}
   
   {:db/ident :token/uref
    :db/valueType :db.type/ref
    :db/cardinality :db.cardinality/one
    :db/doc "User reference"}])

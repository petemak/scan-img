(ns foundation.edn)

;; Reader
;; Clojure is not defined in terms of grammar
;; No lexer that intepretes chrachters to give them a meaning
;; 
;; Reader Reads character and retunrns Clojure data structures
;; Evaluator - evaluates Clojure data structures
;;
;; text   ---> Reader  --(Clojure data structures)-- Evaluator --- *

(read-string "(+ 1 2)")

(list? (read-string "(+ 1 2)"))

(eval (read-string "(+ 1 2)"))


;; Atomic
;; numbers
1
27

;;
;; Integer are auto transformed grow abitrarily with
;; alterntive operators with apostrophe
(*' 10000000000000 100000000)

;;
;; Operators are multivariadic
(* 1 2)
(*)


;;Chars
\a
\v

;;Strings
"Hello"
"Igglepiggle"


;; Keyword
:k
:magapakka

;; vectors
[1 "2" "three"]

;; Lists
'(1 "2" "three")

;; Maps
{:a 1 :b "2" :c "three"}

;; Sets
#{1 "2" "three"}


;;Symbols treated specially by evalutor
;;
;; Treated specially
;; Evaluator treats 1st element as a function
(+ 2 3)

(reduce + (filter even? (range 10)))



;; 







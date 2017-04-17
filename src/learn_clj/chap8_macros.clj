(ns learn-clj.chap8-macros
  [:require [clojure.xml :as xml]])

(macroexpand '(when true true))
;; => (if true (do true))

(defmacro mywhen [condition & body]
  `(when ~condition (do ~@body)))

(macroexpand-1 '(mywhen x true))
;; => (clojure.core/when x (do true))

(macroexpand '(mywhen x true))
;; => (if x (do (do true)))

(let [x 9
      y '(- x)]
  (println `y)
  (println ``y)
  (println `~`y)
  (println `~y)
  (println ``~~y))
;; => nil
;; learn-clj.chap8-macros/y
;; (quote learn-clj.chap8-macros/y)
;; learn-clj.chap8-macros/y
;; (- x)
;; (- x)

;; (domain man-vs-monster
;;         (grouping people
;;                   (Human "A stock human")
;;                   (Man (isa Human)
;;                        "A man, baby"
;;                        [name]
;;                        [has-beard?]))
;;         (grouping monsters
;;                   [Chupacabra
;;                    "A fierce, yet elusive creature"
;;                    [eats-goats]]))

(defn mk-comment [comments]
  (when (not-empty comments)
    {:comment (clojure.string/join "" comments)}))

(defn mk-attrs [attrs]
  (into {}
        (mapv (fn [x]
                [(keyword (first x)) (keyword (second x))]) attrs)))

(defn mk-properties [props]
  (mapv (fn [x]
          {:tag :property
           :attrs {:name (keyword (first x))}})
        props))

(defn mk-details [details]
  (let [details-fn (fn [detail]
                     {:tag :thing
                      :attrs (merge {:name (keyword (first detail))}
                                    (mk-attrs (take-while (comp not string?) (rest detail)))
                                    (mk-comment (filter string? (rest detail))))
                      :content [{:tag :properties
                                 :content (mk-properties (drop-while (comp not vector?) detail))}]
                      })]
    (mapv details-fn details)))

(defmacro grouping [type & details]
  `{:tag :grouping
    :attrs {:name (keyword '~type)}
    :content [~@(mk-details details)]})

(defmacro domain [name & body]
  `{:tag :domain
    :attrs {:name (keyword '~name)}
    :content [~@body]})

(def d
  (-> '(domain man-vs-monster
               (grouping people
                         (Human "A stock human")
                         (Man (isa Human)
                              "A man, baby"
                              [name]
                              [has-beard?]))
               (grouping monsters
                         (Chupacabra
                           "A fierce, yet elusive creature"
                           [eats-goats?])))
      macroexpand
      eval))
;; => {:content [{:content [{:tag :thing, :attrs {:name :Human, :comment "A stock human"}, :content [{:tag :properties, :content []}]} {:tag :thing, :attrs {:name :Man, :isa :Human, :comment "A man, baby"}, :content [{:tag :properties, :content [{:tag :property, :attrs {:name :name}} {:tag :property, :attrs {:name :has-beard?}}]}]}], :attrs {:name :people}, :tag :grouping} {:content [{:tag :thing, :attrs {:name :Chupacabra, :comment "A fierce, yet elusive creature"}, :content [{:tag :properties, :content [{:tag :property, :attrs {:name :eats-goats?}}]}]}], :attrs {:name :monsters}, :tag :grouping}], :attrs {:name :man-vs-monster}, :tag :domain}

(xml/emit d)
;; <?xml version='1.0' encoding='UTF-8'?>
;; <domain name=':man-vs-monster'>
;; <grouping name=':people'>
;; <thing name=':Human' comment='A stock human'>
;; <properties>
;; </properties>
;; </thing>
;; <thing name=':Man' isa=':Human' comment='A man, baby'>
;; <properties>
;; <property name=':name'/>
;; <property name=':has-beard?'/>
;; </properties>
;; </thing>
;; </grouping>
;; <grouping name=':monsters'>
;; <thing name=':Chupacabra' comment='A fierce, yet elusive creature'>
;; <properties>
;; <property name=':eats-goats?'/>
;; </properties>
;; </thing>
;; </grouping>
;; </domain>


(defmacro contextual-eval [ctx expr]
  (eval
    `(let [~@(mapcat (fn [[k v]]
                       [k v])
                     ctx)]
       ~expr)))

(contextual-eval {a 1 b (range)} (apply + a (take 10 b)))

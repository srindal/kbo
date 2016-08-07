(ns kbo.page-dispatcher
  (:require [promesa.core :as p]
            [cats.core :as m]
            [promesa.monad] ;provides monad-support for promises
            [kbo.http-client :as h]))


(defn hent-medlemmer []
  (-> (h/get-basic "/medlemmer")
      (p/then prn)))

(defn frontpage [_]
  {:medlemmer (hent-medlemmer)})


(defn about [_]
  {:hest (p/promise [])})

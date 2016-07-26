(ns kbo.http-client
  (:require [ajax.core :refer [GET POST PUT DELETE PATCH raw-response-format] :as ajax]
            [cljs.core.async :refer [>! <! alts! chan tap untap put! close! mult]]
            [clojure.string]
            [promesa.core :as p]))


                                        ;----------------- Handle sending request ----------


(defn- execute-request-basic [{:keys [accept method uri content headers on-success on-error content-type]
                               :or   {accept       :application+json
                                      content-type :application+json
                                      on-success   (constantly nil)
                                      on-error     (constantly nil)}}]
  (method uri {:params           content
               :handler          #(on-success {
                                               :body    %})
               :error-handler    #(on-error {
                                             :body    %})}))

(defn get-basic
  [params]
  (p/promise
    (fn [resolve reject]
      (execute-request-basic
        {:uri        (clojure.string/join params)
         :method     GET
         :on-success resolve
         :on-error   reject}))))

(ns kbo.state
  (:require [clojure.string]
            [reagent.core :as r]))



(declare
  assoc-in!
  get-model)


; holds all data and state in the application
(defonce state (r/atom {}))


(defn- as-path [path & relative-path]
  (let [p (if (sequential? path) path [path])
        rp (remove nil? relative-path)]
    (if (seq? rp)
      (apply conj p (flatten rp))
      p)))

(defn init-model [path model]
  (swap! state assoc-in path model))


(defn get-model-in
  "Returns the data in the given path"

  [path & relative-path]
  (get-in @state (as-path path relative-path)))

(defn get-model [path]
  (get-in @state path))


(defn assoc-in!
  [path newval]
  (swap! state assoc-in path newval))

(defn update-in!
  [path func & args]
  (apply swap! state update-in path func args))

(declare conj-in!)


(defn exists-in [path val]
  (some #(= val %) (get-model path)))

(defn remove-in!
  "Removes a value in a vector at path"
  [path val]
  (assoc-in! path (vec (remove #(= val %) (get-model path)))))


(defn toggle-value! [path val]
  "Toggle a non-boolean value
  If value exists it is removed - else inserted."
  (if (exists-in path val)
    (remove-in! path val)
    (conj-in! path val)))


(defn- replace-matching-in
  ([state path key newval]
   (let [match? (fn [o k]
                  (fn [v] (= (get v k)
                             (get o k))))
         coll (->> (get-in state path)
                   (remove (match? newval key))
                   (cons newval)
                   vec)]
     (assoc-in state path coll))))

(defn replace-matching-in!
  "Remove any items that match newval
  and replace with newval in a vector at path"
  [path key newval]
  (->> (replace-matching-in @state path key newval)
       (reset! state)))

(defn- conj-in
  [state path val]
  (let [coll (-> (get-in state path)
                 (conj val)
                 vec)]
    (assoc-in state path coll)))

(defn conj-in!
  "Add val to a vector at path"
  [path val]
  (->> (conj-in @state path val)
       (reset! state)))


;; ####################################################################################
;; #####  Path helper functions                                                   #####
;; ####################################################################################

(defn get-active-page-url []
  (get-in @state [:navigation :active-page-url]))

(defn get-active-page []
  (get-in @state [:navigation :active-page]))

(defn page-path
  "Takes a page-url and a relative-path and returns the full path in the :page.
   If no page-url is supplied the path will be in the current path."
  ([]
   (page-path []))
  ([relative-path]
   (page-path (get-active-page-url) relative-path))
  ([page-url & [relative-path]]
   (vec (concat [:page page-url] relative-path))))

(defn page-state-path
  "Takes a relative-path and returns the full path in the :page-state."
  [& [relative-path]]
  (vec (concat [:page-state (get-active-page)] (if (sequential? relative-path) relative-path [relative-path]))))



(defn local-state-path
  "Returns the root path for the local state.
  If no root-path is given the context of the active page url will be used."
  ([]
   (local-state-path (get-active-page-url)))

  ([root-path]
   (as-path :local-state root-path)))



(defn path-to-id [path & ps]
  (clojure.string/join "_" (apply as-path path ps)))



(defn parent
  "Goes op the path tree and returns the full path at the given place."
  ([path]
   (parent path 1))

  ([path nth]
     (subvec path 0 (- (count path) nth))))

(defn grand-parent [path]
  (parent path 2))

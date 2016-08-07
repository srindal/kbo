(ns kbo.utils.util
  "Namespace containing general utility functions."
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [cljs.core.async :refer [>! <! put! take! chan buffer close! alts! timeout]]
            [clojure.string]
            [clojure.set]
            [cljs.reader])
  (:import [goog.fx Dragger]))


;; ####################################################################################
;; #####  General helpers                                                         #####
;; ####################################################################################

(defn dbg [x]
  (prn "dbg" x)
  x)

(defn throw-exception [s]
  (throw (js/Error. s)))

(defn merge-lists
  "Merge a list of lists so that (merge-lists [1 2 3] [\"a\" \"b\" \"c\"]) => ([1 \"a\"] [2 \"b\"] [3 \"c\"])"
  [& lists]
  (apply map vector lists))

(defn reduce-indexed
  "Reduce while adding an index as the second argument to the function"
  ([f coll]
   (reduce-indexed f (first coll) 0 (rest coll)))

  ([f init coll]
   (reduce-indexed f init 0 coll))

  ([f init i coll]
   (if (empty? coll)
     init
     (let [v (first coll)
           fv (f init i v)]
       (recur f fv (inc i) (rest coll))))))

(defn print-object
  ([js-obj]
   (.log js/console js-obj))
  ([msg js-obj]
   (.log js/console msg)
   (.log js/console js-obj)))

(defn toogle [m k]
  (assoc m k (not (get m k))))


(defn last? [index vector]
  (= (- (count vector) 1) index))

(defn index-of
  ([item coll] (index-of 0 item coll =))

  ([item coll condition-fn] (index-of 0 item coll condition-fn))

  ([index item coll condition-fn]
   (cond
     (empty? coll) -1
     (condition-fn item (first coll)) index
     :else (recur (inc index) item (rest coll) condition-fn))))

(defn find-in
  "Takes a collection of maps and returns the first match where (= (k m) v)"
  [coll k v]
  (first (filter #(= (k %) v) coll)))


(defn visible? [node]
  (.-visibility node))

(defn find-first [x coll]
  (->> coll
       (keep-indexed #(when (= x %2) %1))
       first))

(defn ensure-map-not-nil [m]
  (if (nil? m) {} m))

(defn cyclic-previous [item coll]
  (cond
    (nil? item) (last coll)
    (= item (first coll)) (last coll)
    :else (nth coll (dec (index-of item coll)))))

(defn cyclic-next [item coll]
  (cond
    (nil? item) (first coll)
    (= item (last coll)) (first coll)
    :else (nth coll (inc (index-of item coll)))))


(defn next-from [item coll]
  (cond
    (nil? item) (first coll)
    (= item (last coll)) item
    :else (nth coll (inc (index-of item coll)))))

(defn previous-from [item coll]
  (cond
    (nil? item) (last coll)
    (= item (first coll)) item
    :else (nth coll (dec (index-of item coll)))))

(defn update-values
  "Apply a function to each value in a map"
  [m f & args]
  (reduce (fn [r [k v]] (assoc r k (apply f v args))) {} m))

(defn keywordify-map
  "all non-keyword keys are converted to keywords"
  [m]
  (reduce (fn [m-out [k v]] (assoc m-out (if (keyword? k) k (keyword (clojure.string/lower-case k))) v)) {} m))

(defn contains-str [s str]
  (some? (re-find (js/RegExp str "i") s)))

(defn equals-ignore-case [str1 str2]
  (if (or (nil? str1) (nil? str2))
    false
    (= (.toUpperCase str1) (.toUpperCase str2))))

(defn print-time [s f]
  (let [start (.getTime (js/Date.))
        result (f)]
    (println s (- (.getTime (js/Date.)) start))
    result))

(defn memoize-last
  ([f]
   (memoize-last f identical?))
  ([f predicate]
   (let [mem (atom {})]
     (fn [args]
       (let [{:keys [in out]} (get @mem f)]
         (if (predicate args in)
           out
           (let [v (f args)]
             (swap! mem assoc f {:in args :out v})
             v)))))))

(defn vec-remove
  "remove elem in coll"
  [coll pos]
  (vec (concat (subvec coll 0 pos) (subvec coll (inc pos)))))

(defn vec-insert
  "insert elem in coll"
  [coll pos item]
  (if (= pos (count coll))
    (vec (concat coll [item]))
    (vec (concat (subvec coll 0 pos) [item] (subvec coll pos)))))

(defn changed-values
  [a b]
  "Return the map with keys for which the value of a differs from the value of b.
  The values are lists of two items, the old and the new value.
  Eg: {:foo [42 43]
       :bar [nil 117]
       :baz [true nil]}"
  (->> (for [key (concat (keys a) (keys b))
             :when (not= (get b key) (get a key))]
         [key [(get a key) (get b key)]])
       (into {})))

(defn trim [s]
  (when s clojure.string/trim s))

(defn map-vals
  "Takes a function and a map and maps f over the values in the map. Returns a map."
  [f m]
  (into {} (map (fn [[k v]] [k (f v)])) m))

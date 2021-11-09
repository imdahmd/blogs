(ns publisher
  (:require [babashka.fs :as fs]
            [file-helper-functions :as fhf :refer [$p]]
            [clojure.edn :as edn]
            [clojure.string :as string])
  (:import 'java.time.format.DateTimeFormatter
           'java.time.LocalDateTime))

(defonce publish-dir ($p "published"))
(defonce metadata-file ($p "published/.metadata"))

(defn- read-metadata []
  (edn/read-string (slurp metadata-file)))

(defn- now-string []
  (let [date      (LocalDateTime/now)
        formatter (DateTimeFormatter/ofPattern "yyyy-MM-dd HH:mm:ss")]
    (.format date formatter)))

(defn publish [blog-file]
  (let [published-file   (fs/copy blog-file publish-dir)
        metadata         (read-metadata)
        updated-metadata (conj metadata {:blog         (fs/file-name published-file)
                                         :published-at (now-string)})]
    (->> updated-metadata
         pr-str
         (spit metadata-file))))

(defn- equal-ignore-ext [one two]
  (let [onesans (-> one (string/split #"\.") first)
        twosans (-> two (string/split #"\.") first)]
    (= onesans twosans)))

(defn metadata [blog]
  (let [metadata (read-metadata)]
    (first
     (filter #(equal-ignore-ext (:blog %) blog) metadata))))

(comment
  (-> "first-open-source-contribution-to-a-clojure-library.html"
      metadata
      :published-at))

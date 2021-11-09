(ns publisher
  (:require [babashka.fs :as fs]
            [file-helper-functions :as fhf :refer [$p]]
            [clojure.edn :as edn])
  (:import 'java.time.format.DateTimeFormatter
           'java.time.LocalDateTime))

(defonce publish-dir ($p "published"))

(defn now-string []
  (let [date      (LocalDateTime/now)
        formatter (DateTimeFormatter/ofPattern "yyyy-MM-dd HH:mm:ss")]
    (.format date formatter)))

(defn publish [blog-file]
  (fhf/ensure-dir publish-dir)
  (let [metadata-file    (fhf/ensure-file ($p "published/.metadata") "[]")
        published-file   (fs/copy blog-file publish-dir)
        metadata         (edn/read-string (slurp metadata-file))
        updated-metadata (conj metadata {:blog         (fs/file-name published-file)
                                         :published-at (now-string)})]
    (->> updated-metadata
         pr-str
         (spit metadata-file))))

(comment
  (publish "first-open-source-contribution-to-a-clojure-library.md"))

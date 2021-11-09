(ns publisher
  (:require [babashka.fs :as fs]
            [file-helper-functions :as fhf :refer [$p]]))

(defonce publish-dir ($p "published"))

(defn publish [blog-file]
  (fhf/ensure-dir publish-dir)
  (fs/copy blog-file publish-dir))

(comment
  (publish "first-open-source-contribution-to-a-clojure-library.md"))

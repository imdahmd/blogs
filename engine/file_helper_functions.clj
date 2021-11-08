(ns file-helper-functions
  (:require [babashka.fs :as fs]))

(defn ensure-dir [dir-path]
  (if (not (fs/exists? dir-path))
    (fs/create-dir dir-path)
    dir-path))

(defn fetch-files [source-dir ext]
  (if (fs/exists? source-dir)
    (->> (fs/list-dir source-dir)
         (filter #(= ext (fs/extension %)))
         (map fs/file))
    '()))

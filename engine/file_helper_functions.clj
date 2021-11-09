(ns file-helper-functions
  (:require [babashka.fs :as fs]))

(defonce $ROOT_DIR "./")
(defmacro $p [path] `(str $ROOT_DIR ~path))

(defn ensure-dir [dir-path]
  (if (not (fs/exists? dir-path))
    (fs/create-dir dir-path)
    dir-path))

(defn ensure-file [file default]
  (if (not (fs/exists? file))
    (do
      (spit file default)
      (fs/file file))
    (fs/file file)))

(defn fetch-files [source-dir ext]
  (if (fs/exists? source-dir)
    (->> (fs/list-dir source-dir)
         (filter #(= ext (fs/extension %)))
         (map fs/file))
    '()))

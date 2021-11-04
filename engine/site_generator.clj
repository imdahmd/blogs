(ns site-generator
  (:require [babashka.pods :as pods]
            [selmer.parser :as selmer]
            [clojure.string :as string]
            [babashka.fs :as fs]))

(pods/load-pod 'retrogradeorbit/bootleg "0.1.9")
(require '[pod.retrogradeorbit.bootleg.markdown :as md])

(defonce $ROOT_DIR "./")

(defmacro $p [path] `(str $ROOT_DIR ~path))

(defn- md->html [file]
  (let [md   (slurp file)
        html (md/markdown md :data :html)]
    html))

(defn- filename->title [filename]
  (-> filename
      (string/split #"\.")
      first
      (string/replace #"-" " ")
      string/capitalize))

(defn- make-presentable [file]
  (let [title     (filename->title (fs/file-name file))
        body      (md->html file)
        blog-skin (slurp ($p "skin/blog.html"))]
    (selmer/render blog-skin
                   {:title title 
                    :body  body})))

(defn- generate-html [blog-file]
  (let [html-file (-> blog-file
                      (#(string/replace (fs/file-name %) #"\.md" ".html"))
                      fs/file)]
    (do
      (->> blog-file
           make-presentable
           (spit html-file))
      html-file)))

(defn- ensure-dir [dir-path]
  (if (not (fs/exists? dir-path))
    (fs/create-dir dir-path)
    dir-path))

(defn generate [target]
  (let [blog-files (->> (fs/list-dir $ROOT_DIR)
                        (filter #(= "md" (fs/extension %)))
                        (map fs/file))
        dest-dir   (ensure-dir ($p target))]
    (do

      (dorun (map #(-> %
                       generate-html
                       (fs/move dest-dir {:replace-existing 't}))
                  blog-files))

      (fs/copy ($p "skin/style.css") dest-dir {:replace-existing 't}))))

(comment
  (clean-site)
  (generate-site))


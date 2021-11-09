(ns site-generator
  (:require [babashka.pods :as pods]
            [selmer.parser :as selmer]
            [clojure.string :as string]
            [babashka.fs :as fs]
            [file-helper-functions :as fhf :refer [$p]]
            [publisher]))

(pods/load-pod 'retrogradeorbit/bootleg "0.1.9")
(require '[pod.retrogradeorbit.bootleg.markdown :as md])

(defonce index-skin (slurp ($p "skin/index.html")))
(defonce blog-skin (slurp ($p "skin/blog.html")))

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
  (let [title (filename->title (fs/file-name file))
        body  (md->html file)]
    (selmer/render blog-skin
                   {:title title 
                    :body  body})))

(defn- generate-html [blog-file]
  (let [html-file (-> blog-file
                      (#(string/replace (fs/file-name %) #"\.md" ".html"))
                      fs/file)]
    (->> blog-file
         make-presentable
         (spit html-file))
    html-file))

(defn- published-date [blog-file]
  (-> (-> blog-file
          publisher/metadata
          :published-at)
      (string/split #" ")
      first))

(defn- generate-index [html-files]
  (let [list (->> html-files
                  (map fs/file-name)
                  (map #(str "<li>" "<a href=\"" % "\">" (filename->title %) "</a>" " - "  "<span class=\"date\">" (published-date %) "</span>" "</li>"))
                  string/join)]
    (selmer/render index-skin {:list list})))

(defn generate [target]
  (let [blog-files (fhf/fetch-files ($p "published") "md")
        dest-dir   (fhf/ensure-dir ($p target))
        html-files (map #(-> %
                             generate-html
                             (fs/move dest-dir {:replace-existing 't}))
                        blog-files)]
    (spit (str target "/index.html") (generate-index html-files))
    (fs/copy ($p "skin/style.css") dest-dir {:replace-existing 't})))

(comment
  (generate "generated-site"))

(ns cljshortener.handler
  (:use compojure.core)
  (:require [compojure.handler :as handler]
            [compojure.route :as route]
            [monger.core :as mg]
            [monger.collection :as mc]
            [digest]))

(def SERVER "http://localhost:3000/")

;; connect to Mongo on localhost, default port
(mg/connect!)
(mg/set-db! (mg/get-db "cljshortener"))

(defn from-hex-acc [hexdigest acc]
  (if (or (= hexdigest nil) (= hexdigest ""))
    acc
    (let [[c & cs] hexdigest
          ord-c (int c)
          ord-a (int \a)
          ord-0 (int \0)
          x (if (>= ord-c ord-a)
              (+ 10 (- ord-c ord-a))
              (- ord-c ord-0))
          new-acc (+' (*' acc 16) x)]
      (recur cs new-acc))))

(defn from-hex [hexdigest]
  "Convert a hex digest to the bigint it represents."
  (from-hex-acc hexdigest 0))

(defn base62-char [i]
  (let [ord-0 (int \0) ;; 0-9
        ord-a (int \a) ;; 10-35
        ord-A (int \A) ;; 36-61
        ord-c (if (< i 10)
                (+ ord-0 i)
                (if (< i 36)
                  (+ ord-a i -10)
                  (+ ord-A i -36)))]
    (char ord-c)))

(defn base62-encode-acc [n max-chars acc]
  (let [r (mod n 62)
        d (quot n 62)
        c (base62-char r)]
    (if (= max-chars 0)
      (clojure.string/join acc)
      (recur d (- max-chars 1) (cons c acc)))))

(defn base62-encode [n max-chars]
  "Encode a large number in at least max-chars characters of [a-zA-Z0-9]."
  (base62-encode-acc n max-chars ""))

(defn digest62 [url]
  (let [md5 (digest/md5 url)]
    (print md5 "\n")
    (base62-encode (from-hex md5) 22)))

(defn shorten-url [url]
  "Generate a base-62 shortened URL."
  (let [digest (digest62 url)]
    (print digest)))

(defn create-url [url]
  "Create a new shortened URL, unless we've shortened this before."
  (if (= url nil)
    {:status 400 :body "need to POST a url parameter to shorten"}
    (let [existing (mc/find-map-by-id "urls" url)
          short-url (if (= existing nil)
                      (shorten-url url)
                      (existing "short_url"))]
        {:status 201 :body (str SERVER short-url)})))

(defn redirect-to [location]
  [302 {:headers {"Location" location}}])

(defn redirect-from [short-url]
  "Attempt to serve up the short url, if it exists."
  ;; XXX we need to search by a different Mongo attribute
  (let [rec (mc/find-one-as-map "urls" {:short_url short-url})]
    (if (= rec nil)
      (route/not-found "Not found")
      (redirect-to (rec :_id)))))

(defroutes app-routes
  (POST "/create" {params :params} (create-url (params "url"))
  (GET "/:short-url" [short-url]
       (redirect-from short-url))
  (route/not-found "Not Found")))

(def app
  (handler/site app-routes))

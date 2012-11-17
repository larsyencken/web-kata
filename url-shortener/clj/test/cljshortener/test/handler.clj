(ns cljshortener.test.handler
  (:use clojure.test
        ring.mock.request
        cljshortener.handler))

(deftest test-app
  (testing "not-found route"
    (let [response (app (request :get "/invalid"))]
      (print "Status: " response "\n")
      (is (= (:status response) 404)))))

(deftest test-encoding
  (testing "decoding hex"
    (let [result-1 (from-hex "0")
          result-2 (from-hex "9")
          result-3 (from-hex "a")
          result-4 (from-hex "f")
          result-5 (from-hex "10")]
      (is (= result-1 0))
      (is (= result-2 9))
      (is (= result-3 10))
      (is (= result-4 15))
      (is (= result-5 16))))

  (testing "parsing hex digests"
    (let [result-1 (from-hex "0")
          result-2 (from-hex "1")
          result-3 (from-hex "a")
          result-4 (from-hex "f")
          result-5 (from-hex "10")
          result-6 (from-hex "c192d4789adbba5e99c1cafcc5637040")]
      (is (= result-1 0))
      (is (= result-2 1))
      (is (= result-3 10))
      (is (= result-4 15))
      (is (= result-5 16))
      (is (= result-6 85654152623124633122135703325605401281N))))

  (testing "base62-char"
    (let [result-1 (base62-char 0)
          result-2 (base62-char 10)
          result-3 (base62-char 35)
          result-4 (base62-char 36)
          result-5 (base62-char 61)]
      (is (= result-1 \0))
      (is (= result-2 \a))
      (is (= result-3 \z))
      (is (= result-4 \A))
      (is (= result-5 \Z))))

  (testing "digest62"
    (let [result (digest62 "http://99designs.com.au/")]
      (is (= result "1XAS0P0Q1TCtUcGCbNOGTD")))))

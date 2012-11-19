(defproject cljshortener "0.1.0-SNAPSHOT"
  :description "A simple URL shortener in Clojure."
  :url "http://github.com/larsyencken/web-kata"
  :dependencies [[org.clojure/clojure "1.4.0"]
                 [compojure "1.1.1"]
                 [com.novemberain/monger "1.3.4"]
                 [digest "1.3.0"]]
  :plugins [[lein-ring "0.7.3"]]
  :ring {:handler cljshortener.handler/app}
  :profiles
  {:dev {:dependencies [[ring-mock "0.1.3"]]}})

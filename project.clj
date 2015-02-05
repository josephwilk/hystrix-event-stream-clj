(defproject hystrix-event-stream-clj "0.1.4-SNAPSHOT"
  :description "Generate hystrix event streams"
  :url "http://github.com/josephwilk/"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [com.netflix.hystrix/hystrix-clj "1.3.20"]
                 [com.netflix.hystrix/hystrix-metrics-event-stream "1.3.20"]
                 [lamina "0.5.6"]
                 [aleph "0.3.3"]
                 [cheshire "5.4.0"]]

  :profiles {:dev {:dependencies [[midje "1.6.3"]]
                   :plugins      [[lein-midje "3.0.1"]
                                  [lein-cloverage "1.0.2"]
                                  [lein-kibit "0.0.8"]]}})

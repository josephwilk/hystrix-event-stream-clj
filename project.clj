(defproject hystrix-event-stream-clj "0.1.3"
  :description "Generate hystrix event streams"
  :url "http://github.com/josephwilk/"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [com.netflix.hystrix/hystrix-clj "1.3.1"]
                 [com.netflix.hystrix/hystrix-metrics-event-stream "1.1.2"]
                 [lamina "0.5.0-rc3"]
                 [aleph "0.3.0-rc2"]
                 [cheshire "5.2.0"]]

  :profiles {:dev {:dependencies [[midje "1.5.1"]]
                   :plugins      [[lein-midje "3.0.1"]
                                  [lein-cloverage "1.0.2"]
                                  [lein-kibit "0.0.8"]]}})

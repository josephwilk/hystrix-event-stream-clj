(ns hystrix-event-stream-clj.t-metrics
  (:require
   [midje.sweet :refer :all]
   [com.netflix.hystrix.core :as hystrix]
   [hystrix-event-stream-clj.metrics :as metrics]))

(facts "with no hystrix commands run"
  (fact "it generates empty lists"
    (metrics/commands) => []
    (metrics/thread-pools) => []))

(facts "with a hystrix command run"
  (fact "it returns command metrics"
    (hystrix/defcommand testy
      {:hystrix/fallback-fn (constantly nil)}
      [] nil)
    (hystrix/execute #'testy)
    (let [data (metrics/commands)]
      (count data) => 1
      (-> data first :name) => "testy"
      (-> data first :type) => "HystrixCommand"))

  (fact "it returns thread pool metrics"
    (let [data (metrics/thread-pools)]
      (count data) => 1
      (-> data first :type) => "HystrixThreadPool")))
(ns hystrix-event-stream-clj.core
  (:require
   [cheshire.core :as json]
   [aleph.http  :refer :all]
   [lamina.core :refer :all]
   [hystrix-event-stream-clj.metrics :as metrics]))

(def default-delay 2000)

(defn- enqueue-metrics [ch metrics]
  (doseq [metric metrics]
    (enqueue ch (str "\ndata: " (json/generate-string metric) "\n"))))

(defn- write-metrics [ch]
  (try
    (enqueue ch (str "\nping: \n"))
    (enqueue-metrics ch (metrics/commands))
    (enqueue-metrics ch (metrics/thread-pools))
    true
    (catch java.io.IOException e
      false)
    (catch Exception e
      false)))

(defn- metric-streaming [ch]
  (future
    (loop [connected true]
      (Thread/sleep default-delay)
      (when connected (recur (write-metrics ch))))))

(defn- init-stream-channel [ch]
  (receive-all ch (fn [_]))
  (metric-streaming ch))

(defn stream []
  (let [ch (named-channel :hystrix-metric-stream init-stream-channel)]
    {:status 200 :body ch :headers {"Content-Type" "text/event-stream;charset=UTF-8"
                                    "Cache-Control" "no-cache, no-store, max-age=0, must-revalidate"
                                    "Pragma" "no-cache"}}))

(ns hystrix-event-stream-clj.core
  (:require
   [cheshire.core :as json]
   [aleph.http  :refer :all]
   [lamina.core :refer :all]
   [hystrix-event-stream-clj.metrics :as metrics]))

(defn- write-metrics [ch]
  (try
    (enqueue ch (str "\nping: \n"))
    (doall (map #(enqueue ch (str "\ndata: " (json/encode %) "\n")) (metrics/commands)))
    (doall (map #(enqueue ch (str "\ndata: " (json/encode %) "\n")) (metrics/thread-pools)))
    true
    (catch java.io.IOException e
      false)
    (catch Exception e
      false)))

(defn- metric-streaming [ch]
  (future
    (loop [connected true]
      (Thread/sleep 1000)
      (when connected (recur (write-metrics ch))))))

(defn- init-stream-channel [ch]
  (receive-all ch (fn [_]))
  (metric-streaming ch))

(defn stream []
  (let [ch (named-channel :hystrix-metric-stream init-stream-channel)]
    {:status 200 :body ch :headers {"Content-Type" "text/event-stream;charset=UTF-8"
                                    "Cache-Control" "no-cache, no-store, max-age=0, must-revalidate"
                                    "Pragma" "no-cache"}}))
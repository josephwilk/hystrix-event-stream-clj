(ns hystrix-event-stream-clj.response
  (:require
    [aleph.http  :refer :all]
    [lamina.core :refer :all]))

(defn- write-metrics [ch]
  (try
    (enqueue ch (str "\nping: \n"))
    (doall (map #(enqueue ch (str "\ndata: " (json/encode %) "\n")) (metrics/stream)))
    (doall (map #(enqueue ch (str "\ndata: " (json/encode %) "\n")) (metrics/pool-stream)))
    true
    (catch java.io.IOException e 
      false)
    (catch Exception e 
      false)))

(defn- metric-streaming [ch] (future (loop [connected true]
                                      (when connected
                                        (Thread/sleep 1000)
                                        (recur (write-metrics ch))))))

(defn- init-stream-channel [ch]
  (receive-all ch (fn [_]))
  (metric-streaming ch))

(defn stream-metrics []
  (let [ch (named-channel :metric-stream init-stream-channel)]
    {:status 200 :body ch :headers {"Content-Type" "text/event-stream;charset=UTF-8"
                                    "Cache-Control" "no-cache, no-store, max-age=0, must-revalidate"
                                    "Pragma" "no-cache"}}))

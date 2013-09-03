# hystrix-event-stream-clj

[![Build Status](https://travis-ci.org/josephwilk/hystrix-event-stream-clj.png?branch=master)](https://travis-ci.org/josephwilk/hystrix-event-stream-clj)

Easy way to setup a Hystrix (https://github.com/Netflix/Hystrix) event stream without having to use servlets.

## Install

Add to your `project.clj`

https://clojars.org/hystrix-event-stream-clj

## Usage with Netty/Compojure

```clojure
 (:require [hystrix-event-stream-clj.core as hystrix-event])

 (defroutes app (GET "/hystrix.stream" (hystrix-event/stream))
```

Test the event stream by curling:

```
curl locahost:5000/hystrix.stream

data: []

data: []
```

## But Why?

The event stream can be consumed by the Hystrix Dashboard. Giving you pretty mointoring of all circuit breakers.

![Hystrix Dashboard](https://monosnap.com/image/nOFxuqgzQ6evEeGa2iA2r4ANn.png)

## How about with Jetty

With Jetty you can use the Netflix Hystrix Servlet directly. Here is an example of how:

```clojure
(import [com.netflix.hystrix.contrib.metrics.eventstream HystrixMetricsStreamServlet])
(import [org.eclipse.jetty.server Server])
(import [org.eclipse.jetty.servlet ServletContextHandler ServletHolder])

(require '[ring.util.servlet :as servlet])
(require '[ring.adapter.jetty :as jetty])

(defn run-jetty-with-hystrix [app options]
  (let [s (#'jetty/create-server options)
        ^QueuedThreadPool p (QueuedThreadPool. ^Integer (options :max-threads 50))]
    (when (:daemon? options false)
      (.setDaemon p true))
    (doto s
      (.setThreadPool p))
    (when-let [configurator (:configurator options)]
      (configurator s))

    (let [hystrix-holder  (ServletHolder. HystrixMetricsStreamServlet)
          app-holder (ServletHolder. (servlet/servlet app))
          context (ServletContextHandler. s "/" ServletContextHandler/SESSIONS)]
      (.addServlet context hystrix-holder "/hystrix.stream")
      (.addServlet context app-holder "/"))

    (.start s)
    (when (:join? options true)
      (.join s))
    s))


(defroutes app (GET "/hello" {:status 200 :body "Hello"})

(run-jetty-with-hystrix app {:port http-port :join? false})
```

## License

(The MIT License)

Copyright © 2013 Joseph Wilk

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the 'Software'), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED 'AS IS', WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

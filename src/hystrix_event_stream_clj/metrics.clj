(ns hystrix-event-stream-clj.metrics
  (:import [rx Observable Observer Subscription] rx.subscriptions.Subscriptions
           [com.netflix.hystrix Hystrix HystrixExecutable HystrixCommandMetrics HystrixThreadPoolMetrics HystrixCircuitBreaker$Factory HystrixCommandProperties]
           [com.netflix.hystrix.util HystrixRollingNumberEvent])
  (:require
   [aleph.http               :refer :all]
   [cheshire.core            :as json]
   [com.netflix.hystrix.core :as hystrix]
   [lamina.core              :refer :all]))

(defn- command->metrics [^HystrixCommandMetrics cmd]
  (let [key (.getCommandKey cmd)
        circuit-breaker (HystrixCircuitBreaker$Factory/getInstance key)
        healthCounts (.getHealthCounts cmd)
        ^HystrixCommandProperties cmd-properties (.getProperties cmd)]
    {:type "HystrixCommand"
     :name (.name key)
     :group (-> cmd .getCommandGroup .name)
     :currentTime (System/currentTimeMillis)

     :isCircuitBreakerOpen (if-not circuit-breaker false (.isOpen circuit-breaker))

     :errorPercentage  (.getErrorPercentage healthCounts)
     :errorCount (.getErrorCount healthCounts)
     :requestCount (.getTotalRequests healthCounts)

     :rollingCountCollapsedRequests (.getRollingCount cmd HystrixRollingNumberEvent/COLLAPSED)
     :rollingCountExceptionsThrown (.getRollingCount cmd HystrixRollingNumberEvent/EXCEPTION_THROWN)
     :rollingCountFailure (.getRollingCount cmd HystrixRollingNumberEvent/FAILURE)
     :rollingCountFallbackFailure (.getRollingCount cmd HystrixRollingNumberEvent/FALLBACK_FAILURE)
     :rollingCountFallbackRejection (.getRollingCount cmd HystrixRollingNumberEvent/FALLBACK_REJECTION)
     :rollingCountFallbackSuccess (.getRollingCount cmd HystrixRollingNumberEvent/FALLBACK_SUCCESS)
     :rollingCountResponsesFromCache (.getRollingCount cmd HystrixRollingNumberEvent/RESPONSE_FROM_CACHE)
     :rollingCountSemaphoreRejected (.getRollingCount cmd HystrixRollingNumberEvent/SEMAPHORE_REJECTED)
     :rollingCountShortCircuited (.getRollingCount cmd HystrixRollingNumberEvent/SHORT_CIRCUITED)
     :rollingCountSuccess (.getRollingCount cmd HystrixRollingNumberEvent/SUCCESS)
     :rollingCountThreadPoolRejected (.getRollingCount cmd HystrixRollingNumberEvent/THREAD_POOL_REJECTED)
     :rollingCountTimeout (.getRollingCount cmd HystrixRollingNumberEvent/TIMEOUT)

     :currentConcurrentExecutionCount, (.getCurrentConcurrentExecutionCount cmd);

     :latencyExecute_mean (.getExecutionTimeMean cmd)
     :latencyExecute {"0" (.getExecutionTimePercentile cmd 0)
                      "25" (.getExecutionTimePercentile cmd 25)
                      "50" (.getExecutionTimePercentile cmd 50)
                      "75" (.getExecutionTimePercentile cmd 75)
                      "90" (.getExecutionTimePercentile cmd 90)
                      "95" (.getExecutionTimePercentile cmd 95)
                      "99" (.getExecutionTimePercentile cmd 99)
                      "99.5" (.getExecutionTimePercentile cmd 99.5)
                      "100" (.getExecutionTimePercentile cmd 100)}
     :latencyTotal_mean (.getTotalTimeMean cmd)
     :latencyTotal {"0" (.getTotalTimePercentile cmd 0)
                    "25" (.getTotalTimePercentile cmd 25)
                    "50" (.getTotalTimePercentile cmd 50)
                    "75" (.getTotalTimePercentile cmd 75)
                    "90" (.getTotalTimePercentile cmd 90)
                    "95" (.getTotalTimePercentile cmd 95)
                    "99" (.getTotalTimePercentile cmd 99)
                    "99.5" (.getTotalTimePercentile cmd 99.5)
                    "100" (.getTotalTimePercentile cmd 100)
                    }

     :propertyValue_circuitBreakerRequestVolumeThreshold (.. cmd-properties circuitBreakerRequestVolumeThreshold get)
     :propertyValue_circuitBreakerSleepWindowInMilliseconds (.. cmd-properties circuitBreakerSleepWindowInMilliseconds get)
     :propertyValue_circuitBreakerErrorThresholdPercentage (.. cmd-properties circuitBreakerErrorThresholdPercentage get)
     :propertyValue_circuitBreakerForceOpen (.. cmd-properties circuitBreakerForceOpen get)
     :propertyValue_circuitBreakerForceClosed (.. cmd-properties circuitBreakerForceClosed get)
     :propertyValue_circuitBreakerEnabled (.. cmd-properties circuitBreakerEnabled get)

     :propertyValue_executionIsolationStrategy (.. cmd-properties executionIsolationStrategy get toString)
     :propertyValue_executionIsolationThreadTimeoutInMilliseconds (.. cmd-properties executionIsolationThreadTimeoutInMilliseconds get)
     :propertyValue_executionIsolationThreadInterruptOnTimeout (.. cmd-properties executionIsolationThreadInterruptOnTimeout get)
     :propertyValue_executionIsolationThreadPoolKeyOverride (.. cmd-properties executionIsolationThreadPoolKeyOverride get)
     :propertyValue_executionIsolationSemaphoreMaxConcurrentRequests (.. cmd-properties executionIsolationSemaphoreMaxConcurrentRequests get)
     :propertyValue_fallbackIsolationSemaphoreMaxConcurrentRequests (.. cmd-properties fallbackIsolationSemaphoreMaxConcurrentRequests get )

     :propertyValue_metricsRollingStatisticalWindowInMilliseconds (.. cmd-properties metricsRollingStatisticalWindowInMilliseconds get)
     :propertyValue_requestCacheEnabled, (.. cmd-properties requestCacheEnabled get)
     :propertyValue_requestLogEnabled, (.. cmd-properties requestLogEnabled get)

     :reportingHosts 1}))

(defn- thread-pool->metric [^HystrixThreadPoolMetrics pool]
  (let [key (.getThreadPoolKey pool)]

    {:type, "HystrixThreadPool"
     :name, (.name key)
     :currentTime, (System/currentTimeMillis)

     :currentActiveCount (.. pool getCurrentActiveCount intValue)
     :currentCompletedTaskCount (.. pool getCurrentCompletedTaskCount longValue)
     :currentCorePoolSize (.. pool getCurrentCorePoolSize intValue)
     :currentLargestPoolSize (.. pool getCurrentLargestPoolSize intValue)
     :currentMaximumPoolSize (.. pool getCurrentMaximumPoolSize intValue)
     :currentPoolSize (.. pool getCurrentPoolSize intValue)
     :currentQueueSize (.. pool getCurrentQueueSize intValue)
     :currentTaskCount (.. pool getCurrentTaskCount longValue)
     :rollingCountThreadsExecuted (.. pool getRollingCountThreadsExecuted)
     :rollingMaxActiveThreads (.. pool getRollingMaxActiveThreads)

     :propertyValue_queueSizeRejectionThreshold (.. pool getProperties queueSizeRejectionThreshold get)
     :propertyValue_metricsRollingStatisticalWindowInMilliseconds (.. pool getProperties metricsRollingStatisticalWindowInMilliseconds get)

     :reportingHosts 1}))

(defn commands []
  (map command->metrics (HystrixCommandMetrics/getInstances)))

(defn thread-pools []
  (map thread-pool->metric (HystrixThreadPoolMetrics/getInstances)))

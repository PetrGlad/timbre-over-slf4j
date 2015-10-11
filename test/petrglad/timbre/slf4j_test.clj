(ns petrglad.timbre.slf4j-test
  (:require [clojure.test :refer :all]
            [taoensso.timbre :as timbre]
            [petrglad.timbre.slf4j :refer :all])
  (:import (uk.org.lidalia.slf4jtest TestLoggerFactory LoggingEvent TestLogger)
           (java.util Arrays)))

(set! *warn-on-reflection* true)

(defn get-events [^TestLogger logger]
  (vec (.getLoggingEvents logger)))

(defn to-message [^LoggingEvent evt]
  {:level (-> (.getLevel evt)
            (.name)
            (.toLowerCase)
            (keyword))
   :message (.getMessage evt)})

(defn test-logger-fixture [level f]
  (let [logger (TestLoggerFactory/getTestLogger "petrglad.timbre.slf4j-test")]
    (timbre/set-config!
      (assoc timbre/example-config
        :level level
        :appenders {:slf4j (slf4j-appender (constantly logger))}))
    (try
      (f)
      (map to-message (get-events logger))
      (finally (TestLoggerFactory/clear)))))

(def test-msgs
  {:report #(timbre/report "report msg")
   :fatal #(timbre/fatal "fatal msg")
   :error #(timbre/error "error msg")
   :warn #(timbre/warn "warn msg")
   :info #(timbre/info "info msg")
   :debug #(timbre/debug "debug msg")
   :trace #(timbre/trace "trace msg")})

(def level-mapping
  {:error [:report :fatal :error]
   :warn [:warn]
   :info [:info]
   :debug [:debug]
   :trace [:trace]})

(deftest logger-test-msg
  (testing "Messages"
    (doseq [l timbre/ordered-levels]
      (let [events (test-logger-fixture :trace (get test-msgs l))]
        (is (= 1 (count events)))
        (is (.startsWith ^String (:message (first events)) (name l))))))
  (testing "Levels"
    (doseq [i (range (count timbre/ordered-levels))]
      (let [l (get (into [] (reverse timbre/ordered-levels)) i)
            events (test-logger-fixture l (fn []
                                            (doseq [mf (vals test-msgs)]
                                              (mf))))]
        (is (= (inc i) (count events))))))) ;; All up to this level

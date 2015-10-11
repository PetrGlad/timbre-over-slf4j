(ns petrglad.timbre.slf4j
  (:import (org.slf4j Logger LoggerFactory)))

(set! *warn-on-reflection* true)

(defn default-ns-to-logger [^String ?ns-str]
  (LoggerFactory/getLogger (if ?ns-str
                             ?ns-str
                             "timbre.slf4j")))

(defn slf4j-appender
  ([] (slf4j-appender default-ns-to-logger))
  ([ns-to-logger]
   {:enabled? true
    :async? false
    :min-level nil
    :rate-limit nil
    :output-fn (fn [{msg_ :msg_}]
                 (str (force msg_)))
    :fn (fn [{:keys [level ?err_ ?ns-str output-fn] :as data}]
          (let [logger ^Logger (ns-to-logger ?ns-str)]
            (cond
              (and (= level :report)
                (.isErrorEnabled logger)) (.error logger ^String (output-fn data) ^Throwable (force ?err_))
              (and (= level :fatal)
                (.isErrorEnabled logger)) (.error logger ^String (output-fn data) ^Throwable (force ?err_))
              (and (= level :error)
                (.isErrorEnabled logger)) (.error logger ^String (output-fn data) ^Throwable (force ?err_))
              (and (= level :warn)
                (.isWarnEnabled logger)) (.warn logger ^String (output-fn data) ^Throwable (force ?err_))
              (and (= level :info)
                (.isInfoEnabled logger)) (.info logger ^String (output-fn data) ^Throwable (force ?err_))
              (and (= level :debug)
                (.isDebugEnabled logger)) (.debug logger ^String (output-fn data) ^Throwable (force ?err_))
              (and (= level :trace)
                (.isTraceEnabled logger)) (.trace logger ^String (output-fn data) ^Throwable (force ?err_)))))}))

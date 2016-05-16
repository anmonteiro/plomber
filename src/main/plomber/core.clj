(ns plomber.core)

(defn wrap-lifecycle-method [env state class method-name]
  (let [method-name (name method-name)
        make-fn (symbol "plomber.core" (str "wrap-" method-name))]
    `(let [old-method# (goog.object/get (.-prototype ~class) ~method-name)]
       (goog.object/set (.-prototype ~class) ~method-name
          (~make-fn ~state old-method#)))))

(defmacro wrap-lifecycle-methods [state class]
  `(do
     ~@(map (partial wrap-lifecycle-method &env state class)
         [:componentWillMount :componentDidMount
          :componentWillUpdate :componentDidUpdate])))

(defmacro show-when
  ([x] `(show-when ~x identity))
  ([x fn]
   `(if ~x
      (~fn ~x)
      "-")))

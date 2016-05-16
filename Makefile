test:
	lein run -m clojure.main scripts/test-runner.clj
	node target/test/test.js

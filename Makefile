test:
  # https://twitter.com/gavinjoyce/status/691773956144119808
	npm set progress=false
  # stupid react-dom asking for react
	npm install react
	lein with-profile -dev,+test run -m clojure.main scripts/test-runner.clj
	node target/test/test.js

clean:
	rm -rf node_modules
	lein clean


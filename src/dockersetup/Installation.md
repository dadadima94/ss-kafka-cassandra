Author: Guillermo Guiridi https://about.me/erpheus

Installation steps

1. Move the docker-compose.yml, cqlsh.sh, run-producer.sh and run-streaming.sh into the main directory
2. Place the build.sbt file in the src/sparkstreaming directory
3. multiple terminal tabs or windows for the lab in general:
	- In one run `docker-compose up` and then wait until there are no new messages for 10-20s. The first time it will take a while
	- In annother terminal you can connect to cassandra to run queries on it and see the results with `./cqlsh.sh`
	- Another one can be used to run the producer with `./run-producer.sh`
	- Last one to actually run our code `./run-streaming.sh`


5. Testing if everything is setup correctly:
	- Run the producer, you should see a lot of lines coming out with different ProducerRecords. This script has an infinite loop inside so you need to stop it yourself. If the output seems to take a lot of time probably you haven't update the brokers variable properly.
	- Comment all the given code in KafkaSpark and just put `println("hello")` inside main to test that the code is runnable.

# Spark Streaming Stateful Application
### co-author: [Marina Angelovska](https://github.com/marinaangelovska) 

A simple stateful application that readins streaming data from Kafka and stores the result in Cassandra.

The streaming data are (key, value) pairs in the form of "String,int", and we want to calculate the average value of each key and continuously update it, while new pairs arrive. We would also like to store the result in Cassandra continuously. The results are in the form of (key, average value) pairs.
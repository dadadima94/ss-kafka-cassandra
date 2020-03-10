package sparkstreaming

import java.util.HashMap
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.spark.streaming.kafka._
import kafka.serializer.{DefaultDecoder, StringDecoder}
import org.apache.spark.SparkConf
import org.apache.spark.streaming._
import org.apache.spark.streaming.kafka._
import org.apache.spark.storage.StorageLevel
import java.util.{Date, Properties}
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord, ProducerConfig}
import scala.util.Random

import org.apache.spark.sql.cassandra._
import com.datastax.spark.connector._
import com.datastax.driver.core.{Session, Cluster, Host, Metadata}
import com.datastax.spark.connector.streaming._

object KafkaSpark {
  def main(args: Array[String]) {

    val conf = new SparkConf().setAppName("lab2").setMaster("local")
    conf.set("spark.cassandra.connection.host", "cassandra")
    val ssc = new StreamingContext(conf, Seconds(1))
    ssc.checkpoint("checkpoint")


    // connect to Cassandra and make a keyspace and table as explained in the document
    val cluster = Cluster.builder().addContactPoint("cassandra").withPort(9042).build()
    val session = cluster.connect()
    session.execute("CREATE KEYSPACE IF NOT EXISTS avg_space WITH REPLICATION = { 'class' : 'SimpleStrategy', 'replication_factor' : 1 };")
    session.execute("CREATE TABLE IF NOT EXISTS avg_space.avg (word text PRIMARY KEY, count float);")

    // make a connection to Kafka and read (key, value) pairs from it
    val kafkaConf = Map(
      "metadata.broker.list" -> "kafka:9092",
      "zookeeper.connect" -> "zoekeeper:2181",
      "group.id" -> "kafka-spark-streaming",
      "zookeeper.connection.timeout.ms" -> "1000"
    )
    val topics = List("avg").toSet
    val kafkaStream = KafkaUtils.createDirectStream[
                 String, String, StringDecoder, StringDecoder](
                 ssc, kafkaConf, topics
    )
    def parsingFunc(record: (String, String)): (String, Double) = {
        val splitted = record._2.split(",")
        (splitted(0), splitted(1).toDouble)
    }
    val pairs = kafkaStream.map(parsingFunc)

    // measure the average value for each key in a stateful manner
    def mappingFunc(key: String, value: Option[Double], state: State[(Double, Int)]): (String, Double) = {
      val (sum, cnt) = state.getOption.getOrElse((0.0, 0))
      val newSum = value.getOrElse(0.0) + sum
      val newCnt = cnt + 1
      state.update((newSum, newCnt))
      (key, newSum/newCnt)
    }
    val stateDstream = pairs.mapWithState(StateSpec.function(mappingFunc _))

    // store the result in Cassandra
    stateDstream.saveToCassandra("avg_space", "avg", SomeColumns("word", "count"))


    ssc.start()
    ssc.awaitTermination()
  }
}

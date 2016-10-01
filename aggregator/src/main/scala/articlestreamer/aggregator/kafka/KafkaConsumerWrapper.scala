package articlestreamer.aggregator.kafka

import java.util
import java.util.Properties

import com.typesafe.config.ConfigFactory
import org.apache.kafka.clients.consumer.{ConsumerRecord, ConsumerRecords, KafkaConsumer, ConsumerConfig}

import scala.concurrent.duration.Duration
import scala.collection.JavaConversions._

class KafkaConsumerWrapper {

  private val consumer = new KafkaConsumer[String, AnyRef](KafkaConsumerWrapper.properties)

  consumer.subscribe(util.Arrays.asList(KafkaConsumerWrapper.topic))

  def poll(duration: Duration, count: Int): Unit = {
    val millis = duration.toMillis
    val res = (1 to count)
      .flatMap( _ => {
        val l = consumer.poll(millis)
        println("new record : " + l.mkString)
        l
      })
      .foreach(total => println("total : " + total))
  }

  def stopConsumer() = {
    consumer.close()
  }

}

object KafkaConsumerWrapper {

  private val appConfig = ConfigFactory.load()
  val topic = appConfig.getString("kafka.topic")

  val properties = new Properties()
  properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092")
  properties.put(ConsumerConfig.GROUP_ID_CONFIG, "test")
  properties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true")
  properties.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "1000")
  properties.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, "30000")
  properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer")
  properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer")
  properties.put(ConsumerConfig.FETCH_MAX_WAIT_MS_CONFIG, "5000")

}
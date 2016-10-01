package articlestreamer.processor.kafka

import java.util
import java.util.{UUID, Properties}

import com.typesafe.config.ConfigFactory
import org.apache.kafka.clients.consumer.{ConsumerRecord, ConsumerConfig, KafkaConsumer}

import scala.collection.JavaConversions._
import scala.concurrent.duration.Duration

class KafkaConsumerWrapper {

  private val consumer = new KafkaConsumer[String, String](KafkaConsumerWrapper.properties)

  consumer.subscribe(util.Arrays.asList(KafkaConsumerWrapper.topic))

  def poll(duration: Duration, count: Int): List[String] = {

    println("Polling started.")

    val millis = duration.toMillis

    val values: List[String] = (1 to count)
      .flatMap( _ => {
        consumer.poll(millis)
      })
      .foldLeft(List[String]()) ((values: List[String], record: ConsumerRecord[String, String]) => {
        // TODO at the moment elements are added to beginning of list, thus the list is in opposite order
        record.value() :: values
      })

    println("Polling Completed.")

    values
  }

  def stopConsumer() = {
    println("Stopping consumer.")
    consumer.close()
  }

}

object KafkaConsumerWrapper {

  private val appConfig = ConfigFactory.load()
  val topic = appConfig.getString("kafka.topic")

  val properties = new Properties()
  properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092")
  properties.put(ConsumerConfig.GROUP_ID_CONFIG, "test-" + UUID.randomUUID().toString)
  properties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true")
  properties.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "1000")
  properties.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, "30000")
  properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer")
  properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer")
  properties.put(ConsumerConfig.FETCH_MAX_WAIT_MS_CONFIG, "5000")
  properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")

}
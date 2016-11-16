package articlestreamer.aggregator.kafka

import java.util
import java.util.{Properties, UUID}

import articlestreamer.shared.configuration.ConfigLoader
import articlestreamer.shared.kafka.KafkaFactory
import com.typesafe.scalalogging.LazyLogging
import org.apache.kafka.clients.CommonClientConfigs
import org.apache.kafka.clients.consumer.{ConsumerConfig, ConsumerRecord}
import org.apache.kafka.common.config.SslConfigs

import scala.concurrent.duration.Duration

class KafkaConsumerWrapper(config: ConfigLoader, factory: KafkaFactory[String, AnyRef]) extends LazyLogging {

  private val topic = config.kafkaMainTopic

  private val consumer = factory.getConsumer(KafkaConsumerWrapper.getProperties(config))
  consumer.subscribe(util.Arrays.asList(topic))

  def poll(duration: Duration, count: Int): Unit = {
    val millis = duration.toMillis
    for(x <- 1 to count) {
      val l = consumer.poll(millis)
      logger.info(s"new ${l.count()} records : ${printRecords(l.records(topic))}")
    }
  }

  def printRecords(records: java.lang.Iterable[ConsumerRecord[String, AnyRef]]): String = {
    val iter = records.iterator()
    val str = new StringBuffer()
    while(iter.hasNext) {
      str.append(iter.next().value())
    }
    str.toString
  }

  def stopConsumer() = {
    consumer.close()
  }

}

object KafkaConsumerWrapper {

  def getProperties(config: ConfigLoader): Properties = {

    import config._

    val properties = new Properties()
    properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaBrokers)
    properties.put(ConsumerConfig.GROUP_ID_CONFIG, "test-" + UUID.randomUUID().toString)
    properties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true")
    properties.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "1000")
    properties.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, "30000")
    properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer")
    properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer")
    properties.put(ConsumerConfig.FETCH_MAX_WAIT_MS_CONFIG, "5000")
    properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")

    if (kafkaSSLMode) {
      properties.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SSL")
      properties.put(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG, s"$kafkaTrustStore/truststore.jks")
      properties.put(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG, "test1234")
      properties.put(SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG, s"$kafkaTrustStore/keystore.jks")
      properties.put(SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG, "test1234")
      properties.put(SslConfigs.SSL_KEY_PASSWORD_CONFIG, "test1234")
    }

    properties
  }

}
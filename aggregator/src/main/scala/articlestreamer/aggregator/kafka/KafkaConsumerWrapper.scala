package articlestreamer.aggregator.kafka

import java.util
import java.util.Properties

import articlestreamer.shared.configuration.ConfigLoader
import articlestreamer.shared.kafka.KafkaConsumerFactory
import org.apache.kafka.clients.CommonClientConfigs
import org.apache.kafka.clients.consumer.{ConsumerConfig, KafkaConsumer}
import org.apache.kafka.common.config.SslConfigs

import scala.collection.JavaConversions._
import scala.concurrent.duration.Duration

class KafkaConsumerWrapper(config: ConfigLoader) {

  val topic = config.kafkaMainTopic

  private val consumer: KafkaConsumer[String, AnyRef] = new KafkaConsumer[String, AnyRef](KafkaConsumerWrapper.getProperties(config))

  consumer.subscribe(util.Arrays.asList(topic))

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

  def getProperties(config: ConfigLoader): Properties = {

    import config._

    val properties = new Properties()
    properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaBrokers)
    properties.put(ConsumerConfig.GROUP_ID_CONFIG, "test")
    properties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true")
    properties.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "1000")
    properties.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, "30000")
    properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer")
    properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer")
    properties.put(ConsumerConfig.FETCH_MAX_WAIT_MS_CONFIG, "5000")
    properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")

    if (kafkaSSLMode) {
      properties.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SSL")
      properties.put(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG, s"${kafkaTrustStore}/truststore.jks")
      properties.put(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG, "test1234")
      properties.put(SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG, s"${kafkaTrustStore}/keystore.jks")
      properties.put(SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG, "test1234")
      properties.put(SslConfigs.SSL_KEY_PASSWORD_CONFIG, "test1234")
    }

    properties
  }

}
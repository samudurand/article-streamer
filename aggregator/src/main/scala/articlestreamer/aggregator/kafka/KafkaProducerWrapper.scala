package articlestreamer.aggregator.kafka

import java.util.Properties

import articlestreamer.shared.configuration.ConfigLoader
import org.apache.kafka.clients.CommonClientConfigs
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerConfig, ProducerRecord}
import org.apache.kafka.common.config.SslConfigs


class KafkaProducerWrapper() {

  private val producer: KafkaProducer[String, String] = new KafkaProducer[String, String](KafkaProducerWrapper.properties)

  def send(record: ProducerRecord[String, String]) = producer.send(record, new RecordCallback)

  def stopProducer() = {
    producer.close()
  }

}

object KafkaProducerWrapper {

  val properties = new Properties()
  properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, ConfigLoader.kafkaBrokers)
  properties.put(ProducerConfig.ACKS_CONFIG, "all")
  properties.put(ProducerConfig.RETRIES_CONFIG, 0.asInstanceOf[AnyRef])
  properties.put(ProducerConfig.BATCH_SIZE_CONFIG, 16384.asInstanceOf[AnyRef])
  properties.put(ProducerConfig.LINGER_MS_CONFIG, 1.asInstanceOf[AnyRef])
  properties.put(ProducerConfig.BUFFER_MEMORY_CONFIG, 33554432.asInstanceOf[AnyRef])
  properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer")
  properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer")
  properties.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, 2000.asInstanceOf[AnyRef])
  properties.put(ProducerConfig.MAX_BLOCK_MS_CONFIG, 10000.asInstanceOf[AnyRef])

  if (ConfigLoader.kafkaSSLMode) {
    properties.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SSL")
    properties.put(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG, s"${ConfigLoader.kafkaTrustStore}/truststore.jks")
    properties.put(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG, "test1234")
    properties.put(SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG, s"${ConfigLoader.kafkaTrustStore}/keystore.jks")
    properties.put(SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG, "test1234")
    properties.put(SslConfigs.SSL_KEY_PASSWORD_CONFIG, "test1234")
  }

}
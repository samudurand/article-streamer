package articlestreamer.aggregator.kafka

import java.util.Properties

import com.typesafe.config.ConfigFactory
import org.apache.kafka.clients.producer.{ProducerConfig, ProducerRecord, KafkaProducer}

import scala.Int


class KafkaProducerWrapper() {

  private val producer: KafkaProducer[String, String] = new KafkaProducer[String, String](KafkaProducerWrapper.properties)

  def send(record: ProducerRecord[String, String]) = producer.send(record, new RecordCallback)

  def stopProducer() = {
    producer.close()
  }

}

object KafkaProducerWrapper {

  val properties = new Properties()
  properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092")
  properties.put(ProducerConfig.ACKS_CONFIG, "all")
  properties.put(ProducerConfig.RETRIES_CONFIG, 0.asInstanceOf[AnyRef])
  properties.put(ProducerConfig.BATCH_SIZE_CONFIG, 16384.asInstanceOf[AnyRef])
  properties.put(ProducerConfig.LINGER_MS_CONFIG, 1.asInstanceOf[AnyRef])
  properties.put(ProducerConfig.BUFFER_MEMORY_CONFIG, 33554432.asInstanceOf[AnyRef])
  properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer")
  properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer")
  properties.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, 2000.asInstanceOf[AnyRef])
  properties.put(ProducerConfig.MAX_BLOCK_MS_CONFIG, 5000.asInstanceOf[AnyRef])

}
package articlestreamer.shared.kafka

import java.util.Properties

import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.producer.KafkaProducer

class KafkaFactory[K,V] {

  def getProducer(properties: Properties): KafkaProducer[K,V] = {
    new KafkaProducer[K,V](properties)
  }

  def getConsumer(properties: Properties): KafkaConsumer[K,V] = {
    new KafkaConsumer[K,V](properties)
  }

}

package articlestreamer.shared.kafka

import java.util.Properties

import org.apache.kafka.clients.producer.KafkaProducer

class KafkaProducerFactory[K,V] {

  def getProducer(properties: Properties): KafkaProducer[K,V] = {
    new KafkaProducer[K,V](properties)
  }

}

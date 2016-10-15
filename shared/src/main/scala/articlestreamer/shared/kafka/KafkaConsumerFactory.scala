package articlestreamer.shared.kafka

import java.util.Properties

import org.apache.kafka.clients.consumer.KafkaConsumer

trait KafkaConsumerFactory[K,V] {

  def getConsumer(properties: Properties): KafkaConsumer[K,V] = {
    new KafkaConsumer[K,V](properties)
  }

}

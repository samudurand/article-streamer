package articlestreamer.aggregator

import articlestreamer.aggregator.kafka.KafkaConsumerWrapper
import articlestreamer.shared.configuration.ConfigLoader

import scala.concurrent.duration._

/**
 * Used for testing the kafka connection and content
 */
object BasicConsumer extends App {

  override def main(args: Array[String]) {

   val consumer = new KafkaConsumerWrapper(new ConfigLoader())

    println("Starting polling")

    consumer.poll(5 seconds, 10)

    println("Polling stopped")

    consumer.stopConsumer()

  }

}

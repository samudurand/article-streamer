package articlestreamer.aggregator

import articlestreamer.aggregator.kafka.KafkaConsumerWrapper

import scala.concurrent.duration._

/**
 * Used for testing the kafka connection and content
 */
object BasicConsumer extends App {

  override def main(args: Array[String]) {

   val consumer = new KafkaConsumerWrapper

    println("Starting polling")

    consumer.poll(5 seconds, 10)

    println("Polling stopped")

    consumer.stopConsumer()

  }

}

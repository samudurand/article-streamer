package articlestreamer.aggregator

import articlestreamer.aggregator.kafka.KafkaConsumerWrapper
import articlestreamer.shared.configuration.{ConfigLoader, DefaultConfigLoader}
import articlestreamer.shared.kafka.KafkaConsumerFactory
import com.softwaremill.macwire._

import scala.concurrent.duration._

/**
 * Used for testing the kafka connection and content
 */
object BasicConsumer extends App {

  override def main(args: Array[String]) {

    val config = wire[DefaultConfigLoader]
    val factory = wire[KafkaConsumerFactory[String, AnyRef]]
    val consumer = wire[KafkaConsumerWrapper]

    println("Starting polling")

    consumer.poll(5 seconds, 10)

    println("Polling stopped")

    consumer.stopConsumer()

  }

}

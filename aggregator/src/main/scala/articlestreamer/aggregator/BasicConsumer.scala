package articlestreamer.aggregator

import articlestreamer.aggregator.kafka.KafkaConsumerWrapper
import articlestreamer.shared.configuration.DefaultConfigLoader
import articlestreamer.shared.kafka.KafkaFactory
import com.softwaremill.macwire._
import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.duration._

/**
 * Used for testing the kafka connection and content
 */
object BasicConsumer extends App with LazyLogging{

  override def main(args: Array[String]) {

    val config = wire[DefaultConfigLoader]
    val factory = wire[KafkaFactory[String, AnyRef]]
    val consumer = wire[KafkaConsumerWrapper]

    logger.info("Starting polling")

    consumer.poll(5 seconds, 10)

    logger.info("Polling stopped")

    consumer.stopConsumer()

  }

}

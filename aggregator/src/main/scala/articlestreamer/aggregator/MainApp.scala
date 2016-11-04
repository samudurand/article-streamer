package articlestreamer.aggregator

import articlestreamer.aggregator.kafka.KafkaProducerWrapper
import articlestreamer.aggregator.twitter.DefaultTwitterStreamerFactory
import articlestreamer.shared.configuration.DefaultConfigLoader
import articlestreamer.shared.kafka.KafkaProducerFactory
import articlestreamer.shared.scoring.NaiveTwitterScoreCalculator
import articlestreamer.shared.twitter.service.TwitterService
import com.softwaremill.macwire._
import twitter4j.TwitterFactory

/**
  * Created by sam on 15/10/2016.
  */
object MainApp extends App {

  override def main(args: Array[String]) {

    lazy val twitterFactory = wire[TwitterFactory]
    lazy val config = wire[DefaultConfigLoader]
    lazy val producerFactory = wire[KafkaProducerFactory[String, String]]
    lazy val producer = wire[KafkaProducerWrapper]
    lazy val streamFactory = new DefaultTwitterStreamerFactory
    lazy val twitterService = wire[TwitterService]
    lazy val scoreCalculator = wire[NaiveTwitterScoreCalculator]

    val aggregator = wire[Aggregator]
    aggregator.run()

  }

}

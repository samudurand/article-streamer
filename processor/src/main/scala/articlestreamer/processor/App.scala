package articlestreamer.processor

import articlestreamer.processor.spark.OnDemandSparkProvider
import articlestreamer.shared.configuration.DefaultConfigLoader
import articlestreamer.shared.kafka.{HalfDayTopicManager, KafkaFactory}
import articlestreamer.shared.scoring.NaiveTwitterScoreCalculator
import articlestreamer.shared.twitter.service.TwitterService
import com.softwaremill.macwire._
import twitter4j.TwitterFactory

object App extends App {

  lazy val configLoader = wire[DefaultConfigLoader]
  lazy val sparkProvider = wire[OnDemandSparkProvider]

  val processor = wire[Processor]
  processor()

}

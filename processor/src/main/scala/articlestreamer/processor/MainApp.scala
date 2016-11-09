package articlestreamer.processor

import articlestreamer.processor.kafka.KafkaConsumerWrapper
import articlestreamer.processor.spark.OnDemandSparkSessionProvider
import articlestreamer.shared.configuration.DefaultConfigLoader
import articlestreamer.shared.kafka.KafkaFactory
import articlestreamer.shared.scoring.NaiveTwitterScoreCalculator
import articlestreamer.shared.twitter.service.TwitterService
import com.softwaremill.macwire._
import twitter4j.TwitterFactory

object MainApp extends App {

  lazy val configLoader = wire[DefaultConfigLoader]

  lazy val twitterFactory = wire[TwitterFactory]
  lazy val twitterService = wire[TwitterService]
  lazy val consumerFactory = wire[KafkaFactory[String, String]]
  lazy val kafkaConsumerWrapper = wire[KafkaConsumerWrapper]
  lazy val sparkProvider = wire[OnDemandSparkSessionProvider]
  lazy val twitterScoreCalculator = wire[NaiveTwitterScoreCalculator]

  val processor = wire[ArticleProcessor]
  processor.run()

}

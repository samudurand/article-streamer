package articlestreamer.twitterupdater

import articlestreamer.shared.configuration.DefaultConfigLoader
import articlestreamer.shared.kafka.{HalfDayTopicManager, KafkaFactory}
import articlestreamer.shared.scoring.NaiveTwitterScoreCalculator
import articlestreamer.shared.twitter.service.TwitterService
import com.softwaremill.macwire._
import twitter4j.TwitterFactory

object App extends App {

  lazy val configLoader = wire[DefaultConfigLoader]

  lazy val twitterFactory = wire[TwitterFactory]
  lazy val twitterService = wire[TwitterService]
  lazy val consumerFactory = wire[KafkaFactory[String, String]]
  lazy val twitterScoreCalculator = wire[NaiveTwitterScoreCalculator]
  lazy val topicManager = wire[HalfDayTopicManager]

  val scoreUpdater = wire[ScoreUpdater]
  scoreUpdater()

  // TODO shouldn't be needed but KafkaConsumer 0.10.1.0 introduced a bug that prevent
  // TODO the application from shutting down by itself. Might require investigation if not fix provided soon
  System.exit(0)

}

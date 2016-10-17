package articlestreamer.processor

import articlestreamer.processor.kafka.KafkaConsumerWrapper
import articlestreamer.processor.service.TwitterService
import articlestreamer.processor.spark.{OnDemandSparkSessionProvider, SparkSessionProvider}
import articlestreamer.shared.configuration.{ConfigLoader, DefaultConfigLoader}
import com.softwaremill.macwire._
import twitter4j.TwitterFactory

object MainApp extends App {

  override def main(args: Array[String]) = {

    lazy val configLoader = wire[DefaultConfigLoader]

    lazy val twitterFactory = wire[TwitterFactory]
    lazy val twitterService = wire[TwitterService]
    lazy val kafkaConsumerWrapper = wire[KafkaConsumerWrapper]
    lazy val sparProvider = wire[OnDemandSparkSessionProvider]

    val processor = wire[ArticleProcessor]
    processor.run()

  }

}

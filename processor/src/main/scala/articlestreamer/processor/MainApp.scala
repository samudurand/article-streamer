package articlestreamer.processor

import articlestreamer.processor.kafka.KafkaConsumerWrapper
import articlestreamer.processor.service.TwitterService
import articlestreamer.shared.configuration.ConfigLoader
import com.softwaremill.macwire._

object MainApp extends App {

  override def main(args: Array[String]) = {

    lazy val configLoader = wire[ConfigLoader]

    lazy val twitterService = wire[TwitterService]
    lazy val kafkaConsumerWrapper = wire[KafkaConsumerWrapper]

    val processor = wire[ArticleProcessor]
    processor.run()

  }

}

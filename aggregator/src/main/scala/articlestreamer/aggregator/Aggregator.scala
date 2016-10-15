package articlestreamer.aggregator

import java.sql.Timestamp
import java.util.UUID

import articlestreamer.aggregator.kafka.KafkaProducerWrapper
import articlestreamer.aggregator.scoring.NaiveTwitterScoreCalculator
import articlestreamer.aggregator.twitter.TwitterStreamer
import articlestreamer.shared.configuration.ConfigLoader
import articlestreamer.shared.model.TwitterArticle
import com.typesafe.config.ConfigFactory
import org.apache.kafka.clients.producer._
import org.json4s._
import org.json4s.jackson.Serialization
import org.json4s.jackson.Serialization.write
import twitter4j.Status
import com.softwaremill.macwire._

class Aggregator(config: ConfigLoader, scoreCalculator: NaiveTwitterScoreCalculator, producer: KafkaProducerWrapper) {

  def run() {

    val twitterStreamer = TwitterStreamer(config, tweetHandler(producer), stopHandler(producer))

    println("Starting streaming")
    twitterStreamer.startStreaming()

    sys.addShutdownHook({
      println("Stopping streaming")
      twitterStreamer.stop()
      println("Streaming stopped")

      producer.stopProducer()
    })
  }

  def tweetHandler(producer: KafkaProducerWrapper): (Status) => Unit = {
    (status: Status) => {

      println(s"Status received: ${status.getCreatedAt}")

      val appConfig = ConfigFactory.load()
      val topic = config.kafkaMainTopic

      val article = convertToArticle(status)

      implicit val formats = Serialization.formats(NoTypeHints)

      val record = new ProducerRecord[String, String](topic, s"tweet${status.getId}", write(article))
      producer.send(record)

    }
  }

  private def convertToArticle(status: Status): TwitterArticle = {

    val urls: List[String] = status.getURLEntities.map{
      urlEntity => urlEntity.getURL
    }.toList

    val publicationDate = new Timestamp(status.getCreatedAt.getTime)

    val article = TwitterArticle(UUID.randomUUID().toString, String.valueOf(status.getId), publicationDate, urls, status.getText, None)
    val baseScore = scoreCalculator.calculateBaseScore(article)
    article.copy(score = Some(baseScore))

  }

  def stopHandler(producer: KafkaProducerWrapper): () => Unit = {
    () => producer.stopProducer()
  }

}

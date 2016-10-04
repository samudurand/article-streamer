package articlestreamer.aggregator

import java.sql.Timestamp
import java.time.{LocalDate, ZoneId}
import java.util.UUID

import articlestreamer.aggregator.kafka.KafkaProducerWrapper
import articlestreamer.aggregator.twitter.TwitterStreamer
import articlestreamer.shared.model.TwitterArticle
import com.typesafe.config.ConfigFactory
import org.apache.kafka.clients.producer._
import org.json4s.jackson.Serialization
import twitter4j.Status

import org.json4s._
import org.json4s.JsonDSL._
import org.json4s.jackson.JsonMethods._
import org.json4s.jackson.Serialization.write

object MainProducer extends App {

  override def main(args: Array[String]) {

    val producer = new KafkaProducerWrapper

    val twitterStreamer = TwitterStreamer(tweetHandler(producer), stopHandler(producer))

    println("Starting streaming")
    twitterStreamer.startStreaming()

    producer.stopProducer()

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
      val topic = appConfig.getString("kafka.topic")

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

    TwitterArticle(UUID.randomUUID().toString, String.valueOf(status.getId), publicationDate, urls, status.getText, Some(1))

  }

  def stopHandler(producer: KafkaProducerWrapper): () => Unit = {
    () => producer.stopProducer()
  }

}

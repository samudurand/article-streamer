package articlestreamer.aggregator

import java.time.{LocalDate, ZoneId}
import java.util.UUID

import articlestreamer.aggregator.kafka.KafkaProducerWrapper
import articlestreamer.aggregator.twitter.TwitterStreamer
import articlestreamer.shared.model.{TwitterArticle, Article}
import com.typesafe.config.ConfigFactory
import org.apache.kafka.clients.producer._
import twitter4j.Status

object MainProducer extends App {

  override def main(args: Array[String]) {

    val producer = new KafkaProducerWrapper

    val twitterStreamer = TwitterStreamer(tweetHandler(producer), stopHandler(producer))

    println("Starting streaming")
    twitterStreamer.startStreaming()

    Thread.sleep(10000)

    println("Stopping streaming")
    twitterStreamer.stop()
    println("Streaming stopped")

//    val record = new ProducerRecord[String, String]("tweets", "tweet" + Random.nextInt(), "tweet_value_" + Random.nextInt())
//    producer.send(record)

    producer.stopProducer()

  }

  def tweetHandler(producer: KafkaProducerWrapper): (Status) => Unit = {
    (status: Status) => {

      import scala.pickling.Defaults._
      import scala.pickling.json._

      println(s"Status received: ${status.getCreatedAt}")

      val appConfig = ConfigFactory.load()
      val topic = appConfig.getString("kafka.topic")

      val article = convertToArticle(status)

      val record = new ProducerRecord[String, String](topic, s"tweet${status.getId}", article.pickle.value)
      producer.send(record)

    }
  }

  private def convertToArticle(status: Status): Article = {

    val urls: List[String] = status.getURLEntities.map{
      urlEntity => urlEntity.getURL
    }.toList

    val creationDate: LocalDate = status.getCreatedAt.toInstant.atZone(ZoneId.of("UTC")).toLocalDate

    TwitterArticle(UUID.randomUUID(), String.valueOf(status.getId), creationDate, urls, status.getText, Some(1))

  }

  def stopHandler(producer: KafkaProducerWrapper): () => Unit = {
    () => producer.stopProducer()
  }

}

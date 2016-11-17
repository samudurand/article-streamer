package articlestreamer.aggregator

import java.sql.Date
import java.util.{Calendar, TimeZone, UUID}

import articlestreamer.aggregator.kafka.KafkaProducerWrapper
import articlestreamer.aggregator.twitter.TwitterStreamerFactory
import articlestreamer.aggregator.twitter.utils.TwitterStatusMethods
import articlestreamer.shared.Constants
import articlestreamer.shared.configuration.ConfigLoader
import articlestreamer.shared.marshalling.CustomJsonFormats
import articlestreamer.shared.model.{TweetAuthor, TwitterArticle}
import articlestreamer.shared.scoring.TwitterScoreCalculator
import com.typesafe.scalalogging.LazyLogging
import org.apache.kafka.clients.producer._
import org.json4s.jackson.Serialization.write
import twitter4j.Status

class Aggregator(config: ConfigLoader,
                 producer: KafkaProducerWrapper,
                 scoreCalculator: TwitterScoreCalculator,
                 streamer: TwitterStreamerFactory) extends CustomJsonFormats with TwitterStatusMethods with LazyLogging {

  def run() = {

    val twitterStreamer = streamer.getStreamer(config, tweetHandler(producer), stopHandler(producer))

    logger.info("Starting streaming")
    twitterStreamer.startStreaming()

    sys.addShutdownHook({
      logger.info("Stopping streaming")
      twitterStreamer.stop()
      logger.info("Streaming stopped")

      producer.stopProducer()
    })
  }

  private def tweetHandler(producer: KafkaProducerWrapper): (Status) => Unit = {
    (status: Status) => {

      logger.info(s"Status received: ${status.getCreatedAt}")

      if (!status.isRetweet && status.isPotentialArticle) {
        val article = convertToArticle(status)

        val record = new ProducerRecord[String, String](
          config.kafkaMainTopic,
          s"tweet-${status.getId}",
          write(article))

        producer.send(record)
        producer.send(endOfQueueRecord())

      } else {
        logger.warn(s"Tweet ${status.getId} ignored : '${status.getText.mkString}'")
      }
    }
  }

  private def endOfQueueRecord(): ProducerRecord[String, String] = {
    val currenttime = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
    val currendDate = new Date(currenttime.getTime.getTime)
    new ProducerRecord[String, String](
      config.kafkaMainTopic,
      Constants.END_OF_KEUE_KEY, "")
  }

  private def convertToArticle(status: Status): TwitterArticle = {

    val author = status.getUser

    val urls: List[String] = status.getURLEntities.map{
      urlEntity => urlEntity.getExpandedURL
    }.toList

    val article = TwitterArticle(
      UUID.randomUUID().toString,
      String.valueOf(status.getId),
      new Date(status.getCreatedAt.getTime),
      urls,
      status.getText,
      TweetAuthor(author.getId, author.getScreenName, author.getFollowersCount),
      None)

    val baseScore = scoreCalculator.calculateBaseScore(article)
    article.copy(score = Some(baseScore))

  }

  private def stopHandler(producer: KafkaProducerWrapper): () => Unit = {
    () => producer.stopProducer()
  }

}

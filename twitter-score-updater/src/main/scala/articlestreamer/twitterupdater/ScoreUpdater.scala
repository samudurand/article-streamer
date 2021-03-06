package articlestreamer.twitterupdater

import articlestreamer.shared.configuration.ConfigLoader
import articlestreamer.shared.kafka.{DualTopicManager, KafkaFactory, KafkaProducerWrapper}
import articlestreamer.shared.marshalling.CustomJsonFormats
import articlestreamer.shared.model.TwitterArticle
import articlestreamer.shared.scoring.TwitterScoreCalculator
import articlestreamer.twitterupdater.kafka.KafkaConsumerWrapper
import com.typesafe.scalalogging.LazyLogging
import org.apache.kafka.clients.producer.ProducerRecord
import org.json4s.jackson.Serialization.write

class ScoreUpdater(config: ConfigLoader,
                   factory: KafkaFactory[String, String],
                   scoreCalculator: TwitterScoreCalculator,
                   topicManager: DualTopicManager) extends CustomJsonFormats with LazyLogging {

  val consumer1 = new KafkaConsumerWrapper(config, factory, topicManager.getFirstTopic())
  val consumer2 = new KafkaConsumerWrapper(config, factory, topicManager.getSecondTopic())
  val producer = new KafkaProducerWrapper(config, factory)

  sys.addShutdownHook {
    logger.info("Stopping consumers and producer.")
    consumer1.stopConsumer()
    consumer2.stopConsumer()
    producer.stopProducer()
  }

  def apply(): List[TwitterArticle] = {

    val records = getRecordsFromSource

    if (records.nonEmpty) {

      logger.info(s"Updating scores of ${records.length} tweets")

      val updatedTweets = processArticles(records)
      updatedTweets.foreach(a => logger.info(s"Tweet ${a.originalId} \n" +
        s"Score : ${a.score} \n" +
        s"Date : ${a.publicationDate} \n" +
        s"Content : ${a.content} \n"))

      updatedTweets
    } else {
      logger.info("No tweet found, terminating program")
      List()
    }
  }

  private def processArticles(articles: List[TwitterArticle]): List[TwitterArticle] = {

    // Grouped to fit twitter limitations
    articles
      .grouped(config.twitterConfig.batchSize)
      .flatMap { batch =>
        try {
          val mappedBatch = batch.map( article => (article.originalId.toLong, article)).toMap
          val updatedArticles = scoreCalculator.updateScores(mappedBatch)

          // Store updated messages
          updatedArticles.foreach(sendToKafka)

          updatedArticles
        } catch {
          case ex: Exception =>
            logger.error("Error while updating scores.", ex)
            List()
        }
      }.toList

  }

  private def sendToKafka(article: TwitterArticle) = {
    val record = new ProducerRecord[String, String](
      config.kafkaArticlesTopic,
      s"tweet-${article.id}",
      write(article))

    producer.send(record)
  }

  private def getRecordsFromSource: List[TwitterArticle] = {
    if (topicManager.getCurrentTopic() == topicManager.getSecondTopic()) {
      consumer1.pullAll()
    } else {
      consumer2.pullAll()
    }
  }

}
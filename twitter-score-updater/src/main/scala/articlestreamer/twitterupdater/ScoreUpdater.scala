package articlestreamer.twitterupdater

import articlestreamer.shared.configuration.ConfigLoader
import articlestreamer.shared.kafka.{DualTopicManager, KafkaFactory}
import articlestreamer.shared.model.TwitterArticle
import articlestreamer.shared.scoring.TwitterScoreCalculator
import articlestreamer.twitterupdater.kafka.KafkaConsumerWrapper
import com.typesafe.scalalogging.LazyLogging

class ScoreUpdater(config: ConfigLoader,
                   consumerFactory: KafkaFactory[String, String],
                   scoreCalculator: TwitterScoreCalculator,
                   topicManager: DualTopicManager) extends LazyLogging {

  val ARTICLE_TABLE = "article"

  val consumer1 = new KafkaConsumerWrapper(config, consumerFactory, topicManager.getFirstTopic())
  val consumer2 = new KafkaConsumerWrapper(config, consumerFactory, topicManager.getSecondTopic())

  sys.addShutdownHook {
    consumer1.stopConsumer()
    consumer2.stopConsumer()
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
      .grouped(config.tweetsBatchSize)
      .flatMap { batch =>
        try {
          val mappedBatch = batch.map( article => (article.originalId.toLong, article)).toMap
          scoreCalculator.updateScores(mappedBatch)

          //TODO send to new kafka topic
        } catch {
          case ex: Exception =>
            logger.error("Error while updating scores.", ex)
            List()
        }
      }.toList

  }

  private def getRecordsFromSource: List[TwitterArticle] = {
    if (topicManager.getCurrentTopic() == topicManager.getSecondTopic()) {
      consumer1.pullAll()
    } else {
      consumer2.pullAll()
    }
  }

}


/*

#Datasets

  https://databricks.com/blog/2016/01/04/introducing-apache-spark-datasets.html

#Rdd dataframe dataset

  https://databricks.com/blog/2016/07/14/a-tale-of-three-apache-spark-apis-rdds-dataframes-and-datasets.html

#Convert list/rdd to dataset

  http://stackoverflow.com/a/37513784/1660475

#Various

  https://jaceklaskowski.gitbooks.io/mastering-apache-spark/content/spark-logging.html
  https://metabroadcast.com/blog/resetting-kafka-offsets
  http://www.cakesolutions.net/teamblogs/spark-streaming-tricky-parts
  http://why-not-learn-something.blogspot.co.uk/2016/05/apache-spark-streaming-how-to-do.html

*/
package articlestreamer.processor

import java.util

import articlestreamer.processor.spark.SparkSessionProvider
import articlestreamer.shared.configuration.ConfigLoader
import articlestreamer.shared.kafka.{DualTopicManager, KafkaFactory}
import articlestreamer.shared.model.TwitterArticle
import articlestreamer.shared.model.db.TwitterArticleRow
import articlestreamer.shared.scoring.TwitterScoreCalculator
import articlestreamer.twitterupdater.kafka.KafkaConsumerWrapper
import com.typesafe.scalalogging.LazyLogging
import org.apache.spark.sql.SaveMode

class Processor(config: ConfigLoader,
                consumerFactory: KafkaFactory[String, String],
                scoreCalculator: TwitterScoreCalculator,
                sparkSessionProvider: SparkSessionProvider,
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

      logger.info(s"Processing ${records.length} articles")

      val sortedArticles = processArticles(records)
      sortedArticles.foreach(a => logger.info(s"Article ${a.originalId} \n" +
        s"Score : ${a.score} \n" +
        s"Date : ${a.publicationDate} \n" +
        s"Content : ${a.content} \n"))

      sortedArticles
    } else {
      logger.info("No article recovered, terminating program")
      List()
    }
  }

  private def processArticles(articles: List[TwitterArticle]): List[TwitterArticle] = {
    val sparkSession = sparkSessionProvider.getSparkSession()

    import sparkSession.implicits._

    // Grouped to fit twitter limitations
    val updatedArticles = articles
      .grouped(config.tweetsBatchSize)
      .flatMap { batch =>
        try {
          val mappedBatch = batch.map( article => (article.originalId.toLong, article)).toMap
          scoreCalculator.updateScores(mappedBatch)
        } catch {
          case ex: Exception =>
            logger.error("Error while updating scores.", ex)
            List()
        }
      }.toList

    val sortedDs = sparkSession
      .createDataset(updatedArticles)
      .sort($"score".desc)

    sortedDs.cache()

    val connectionProp = new util.Properties()
    connectionProp.put("user", config.mysqlConfig.user)
    connectionProp.put("password", config.mysqlConfig.password)
    connectionProp.put("useSSL", "false")

    sortedDs
      .map(article => new TwitterArticleRow(article))
      .write
      .mode(SaveMode.Append)
      .jdbc(config.mysqlConfig.jdbcUrl, ARTICLE_TABLE, connectionProp)

    sortedDs.collect().toList
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
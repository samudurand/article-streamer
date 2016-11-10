package articlestreamer.processor

import articlestreamer.processor.kafka.KafkaConsumerWrapper
import articlestreamer.processor.marshalling.TwitterMarshaller.unmarshallTwitterArticle
import articlestreamer.processor.spark.SparkSessionProvider
import articlestreamer.shared.configuration.ConfigLoader
import articlestreamer.shared.model.TwitterArticle
import articlestreamer.shared.scoring.TwitterScoreCalculator
import com.typesafe.scalalogging.LazyLogging
import org.apache.spark.sql.Dataset

import scala.concurrent.duration._
import scala.language.postfixOps

class ArticleProcessor(config: ConfigLoader,
                       consumer: KafkaConsumerWrapper,
                       scoreCalculator: TwitterScoreCalculator,
                       sparkSessionProvider: SparkSessionProvider) extends LazyLogging {

  def run(): List[TwitterArticle] = {

    val records = getRecordsFromSource

    if (records.nonEmpty) {
      logger.info(s"Preparing ${records.length} articles for processing")

      val articles = parseArticles(records)

      logger.info(s"Processing ${articles.length} articles")
      val updatedArticles = processScores(articles)
      val sortedArticles = updatedArticles.sortBy(a => a.score.get)

      sortedArticles.foreach(a => logger.info(s"Article ${a.originalId} \nScore : ${a.score} \nContent : ${a.content} \n"))
      sortedArticles
    } else {
      logger.info("No article recovered, terminating program")
      List()
    }
  }

  def parseArticles(records: List[String]): List[TwitterArticle] = {
    val sparkSession = sparkSessionProvider.getSparkSession()

    import sparkSession.implicits._

    val recordsDs: Dataset[String] = sparkSession.createDataset(records)

    recordsDs
      .flatMap { record =>
        val maybeArticle = unmarshallTwitterArticle(record)
        if (maybeArticle.isEmpty) {
          //TODO At the moment uses simple print for serialization purpose, need to store those errors somewhere else
          println(s"Could not parse record $record into an article.")
        }
        maybeArticle
      }
      .collect().toList
  }

  private def getRecordsFromSource: List[String] = {
    val recordsValues: List[String] = consumer.poll(5 seconds, 10)
    consumer.stopConsumer()
    recordsValues
  }

  private def processScores(articles: List[TwitterArticle]): List[TwitterArticle] = {
    try {

      val articlesById = articles.map(article => (article.originalId.toLong, article)).toMap

      articlesById
        .grouped(config.tweetsBatchSize)
        .flatMap(articleGroup => scoreCalculator.updateScores(articleGroup))
        .toList

    } catch {
      case ex: Throwable =>
        logger.error("Error while processing scores.", ex)
        List()
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
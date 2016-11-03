package articlestreamer.processor

import articlestreamer.processor.kafka.KafkaConsumerWrapper
import articlestreamer.processor.marshalling.TwitterMarshaller.unmarshallTwitterArticle
import articlestreamer.processor.spark.SparkSessionProvider
import articlestreamer.shared.configuration.ConfigLoader
import articlestreamer.shared.exception.exceptions._
import articlestreamer.shared.model.TwitterArticle
import articlestreamer.shared.scoring.TwitterScoreCalculator
import org.apache.log4j.{Level, Logger}
import org.apache.spark.sql.Dataset

import scala.concurrent.duration._

class ArticleProcessor(config: ConfigLoader,
                       consumer: KafkaConsumerWrapper,
                       scoreCalculator: TwitterScoreCalculator,
                       sparkSessionProvider: SparkSessionProvider) {

  Logger.getLogger("org").setLevel(Level.WARN)

  def run() {

    val records = getRecordsFromSource
    val articles = parseArticles(records)
    val updatedArticles = processScores(articles)

    updatedArticles.sortBy(a => a.score)
      .foreach(a => println(s"Article ${a.originalId} \nScore : ${a.score} \nContent : ${a.content} \n"))
  }

  def parseArticles(records: List[String]): List[TwitterArticle] = {
    val sparkSession = sparkSessionProvider.getSparkSession()
    import sparkSession.implicits._

    val recordsDs: Dataset[String] = sparkSession.createDataset(records)

    recordsDs
      .map { record =>
        val maybeArticle = unmarshallTwitterArticle(record)
        if (maybeArticle.isEmpty) {
          System.err.println(s"Could not parse record $record into an article.")
        }
        maybeArticle
      }
      .filter(_.isDefined)
      .map(_.get)
      .collect().toList
  }

  private def getRecordsFromSource: List[String] = {
    val recordsValues: List[String] = consumer.poll(10 seconds, 1)
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
        ex.printNeatStackTrace()
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
package articlestreamer.processor

import articlestreamer.processor.kafka.KafkaConsumerWrapper
import articlestreamer.processor.spark.SparkSessionProvider
import articlestreamer.shared.configuration.ConfigLoader
import articlestreamer.shared.model.TwitterArticle
import articlestreamer.shared.scoring.TwitterScoreCalculator
import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.duration._
import scala.language.postfixOps

class ArticleProcessor(config: ConfigLoader,
                       consumer: KafkaConsumerWrapper,
                       scoreCalculator: TwitterScoreCalculator,
                       sparkSessionProvider: SparkSessionProvider) extends LazyLogging {

  def run(): List[TwitterArticle] = {

    val records = getRecordsFromSource

    if (records.nonEmpty) {

      logger.info(s"Processing ${records.length} articles")

      val sortedArticles = processArticles(records)
      sortedArticles.foreach(a => logger.info(s"Article ${a.originalId} \nScore : ${a.score} \nContent : ${a.content} \n"))
      sortedArticles
    } else {
      logger.info("No article recovered, terminating program")
      List()
    }
  }

  def processArticles(articles: List[TwitterArticle]): List[TwitterArticle] = {
    val sparkSession = sparkSessionProvider.getSparkSession()

    import sparkSession.implicits._

    // Grouped to fit twitter limitations
    val groupedArticles = articles
      .grouped(config.tweetsBatchSize).toList

    val ds = sparkSession.createDataset(groupedArticles)
    ds.flatMap { batch =>
        val mappedBatch = batch.map( article => (article.originalId.toLong, article)).toMap
        val updated = scoreCalculator.updateScores(mappedBatch)
        updated
      }
      .sort($"score")
      .collect().toList
  }

  private def getRecordsFromSource: List[TwitterArticle] = {
    val recordsValues: List[TwitterArticle] = consumer.pullAll(5 seconds, 10)
    consumer.stopConsumer()
    recordsValues
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
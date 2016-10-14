package articlestreamer.processor

import articlestreamer.processor.marshalling.ArticleMarshaller
import articlestreamer.processor.kafka.KafkaConsumerWrapper
import articlestreamer.processor.model.TweetPopularity
import articlestreamer.processor.service.TwitterService
import articlestreamer.shared.configuration.ConfigLoader
import articlestreamer.shared.exception.exceptions._
import articlestreamer.shared.model.{TwitterArticle, Article}
import com.typesafe.config.ConfigFactory

import scala.concurrent.duration._

//import org.apache.kafka.common.serialization.{StringDeserializer, StringSerializer}
import org.apache.spark.SparkContext
import org.apache.spark.SparkContext._
import org.apache.spark.SparkConf
import org.apache.spark._

import org.apache.log4j.{Level, Logger}

import org.apache.spark.sql.{Dataset, SparkSession}

object ArticleProcessor extends ArticleMarshaller with TwitterService with ConfigLoader {

  Logger.getLogger("org").setLevel(Level.WARN)

  def main(args: Array[String]) {

    val records = getRecordsFromSource

    val config = new SparkConf()
      .setAppName("Spark App")
      .setMaster("local[2]")
      .set("spark.streaming.stopGracefullyOnShutdown","true")

    val sparkSession = SparkSession
      .builder()
      .config(config)
      .getOrCreate()

    import sparkSession.implicits._

    val recordsDs: Dataset[String] = sparkSession.createDataset(records)

    val articles = recordsDs.map { record =>
      val maybeArticle = unmarshallTwitterArticle(record)
      if (maybeArticle.isEmpty) {
        System.err.println(s"Could not parse record $record into an article.")
      }
      maybeArticle
    }
    .filter(_.isDefined)
    .map(_.get)
    .collect()
    .toList

    val updatedArticles = processScores(articles)

    updatedArticles.sortBy(a => a.score)
      .foreach(a => println(s"Article ${a.originalId} \nScore : ${a.score} \nContent : ${a.content} \n"))

//    val ssc = new StreamingContext(config, Seconds(1))
//
//    val consumerStrategy = Subscribe[String, String](Set(topic), KafkaConsumerWrapper.properties.asScala.toMap)
//    val messages = KafkaUtils.createDirectStream[String, String](ssc, PreferConsistent, consumerStrategy)
//
//    messages.foreachRDD { message =>
//      println(message.count())
//    }
//
//    ssc.start()
//    ssc.awaitTerminationOrTimeout(streamingTimeout)

//    val conf = new SparkConf().setMaster("local[2]").setAppName("NetworkWordCount")
//    val ssc = new StreamingContext(conf, Seconds(1))

//

//
//    var offsetRanges = Array[OffsetRange]()
//
//    directKafkaStream.transform { rdd =>
//      offsetRanges = rdd.asInstanceOf[HasOffsetRanges].offsetRanges
//      rdd
////    }.map {
////      ...
//    }.foreachRDD { rdd =>
//      for (o <- offsetRanges) {
//        println(s"${o.topic} ${o.partition} ${o.fromOffset} ${o.untilOffset}")
//      }
//    }

//    val logFile = "YOUR_SPARK_HOME/README.md" // Should be some file on your system
//    val conf = new SparkConf().setAppName("Simple Application")
//    val sc = new SparkContext(conf)
//    val logData = sc.textFile(logFile, 2).cache()
//    val numAs = logData.filter(line => line.contains("a")).count()
//    val numBs = logData.filter(line => line.contains("b")).count()
//    println("Lines with a: %s, Lines with b: %s".format(numAs, numBs))
  }

  private def getRecordsFromSource: List[String] = {
    val kafkaConsumer = new KafkaConsumerWrapper
    val recordsValues: List[String] = kafkaConsumer.poll(10 seconds, 1)
    kafkaConsumer.stopConsumer()
    recordsValues
  }

  private def processScores(articles: List[TwitterArticle]): List[TwitterArticle] = {
    try {
      val articlesById = articles.map(article => (article.originalId.toLong, article)).toMap

      val updatedArticles = articlesById
        .grouped(tweetsBatchSize)
        .flatMap(articleGroup => updateScore(articleGroup))
        .toList

      if (updatedArticles.size != articles.size) {
        System.err.println("Something went wrong. Could not update the score of every article.")
      }

      updatedArticles

    } catch {
      case ex: Throwable =>
        ex.printNeatStackTrace()
        List()
    }
  }

  private def updateScore(articlesById: Map[Long, TwitterArticle]): Iterable[TwitterArticle] = {
    getTweetsDetails(articlesById.keys.toList).map {
      case (id, Some(details)) => {
        val article = articlesById(id)
        val updatedScore = calculateTweetScore(article, details)
        article.copy(score = Some(updatedScore))
      }
      case (id, None) => articlesById(id)
    }
  }

  // Calculate a naive score
  private def calculateTweetScore(article: TwitterArticle, popularity: TweetPopularity): Int = {
    article.score.getOrElse(0) + popularity.retweetCount + popularity.favoriteCount * 2
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
package articlestreamer.processor

import _root_.kafka.serializer.{StringEncoder, StringDecoder}
import articlestreamer.processor.marshalling.ArticleMarshaller
import articlestreamer.processor.kafka.KafkaConsumerWrapper
import articlestreamer.processor.service.TwitterService
import articlestreamer.shared.exception.exceptions._
import articlestreamer.shared.model.{TwitterArticle, BaseArticle, Article}
import com.typesafe.config.ConfigFactory
import org.apache.kafka.common.serialization.{StringDeserializer, StringSerializer}

import scala.concurrent.duration._

//import org.apache.kafka.common.serialization.{StringDeserializer, StringSerializer}
import org.apache.spark.SparkContext
import org.apache.spark.SparkContext._
import org.apache.spark.SparkConf
import org.apache.spark._

import org.apache.log4j.{Level, Logger}

import org.apache.spark.sql.{Dataset, SparkSession}
import org.apache.spark.streaming._
import org.apache.spark.streaming.kafka010._
import scala.collection.JavaConverters._
import org.apache.spark.streaming.kafka010.LocationStrategies.PreferConsistent
import org.apache.spark.streaming.kafka010.ConsumerStrategies.Subscribe

object MainApp extends ArticleMarshaller with TwitterService {

  private val appConfig = ConfigFactory.load()
  val topic = appConfig.getString("kafka.topic")
  val streamingTimeout = appConfig.getLong("spark.streamingTimeout")

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

    recordsDs.foreach { record =>
      unmarshallArticle(record) match {
        case twitterArticle: Some[TwitterArticle] => processTwitterArticle(twitterArticle.get)
        case article: Some[Article] =>
          println(s"Article ignored : no processing planned for type of article [${article.get.getClass.getCanonicalName}] yet.")
        case None => System.err.println("Could not parse article.")
      }

      //println(response)
    }

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

  private def processTwitterArticle(article: TwitterArticle) {
    try {
      val twitterId = article.originalId.toLong

      getTweet(twitterId) match {
        case Some(tweet) => println(s"Retrieved details for tweet [$twitterId] : \n $tweet")
        case None =>
          println(s"Unable to retrieve tweet details for article [${article.id}] with tweet id [${article.originalId}]")
      }

    } catch {
      case _: NumberFormatException =>
        System.err.println(s"Unexpected format for twitter Id, cannot convert ${article.originalId} to Long.")
      case ex: Throwable =>
        ex.printNeatStackTrace()
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
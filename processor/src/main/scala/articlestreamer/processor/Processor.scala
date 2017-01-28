package articlestreamer.processor

import java.sql.{DriverManager, SQLException}

import articlestreamer.processor.spark.SparkProvider
import articlestreamer.shared.configuration.ConfigLoader
import articlestreamer.shared.marshalling.TwitterArticleMarshaller
import articlestreamer.shared.model.db.TwitterArticleRow
import com.typesafe.scalalogging.Logger
import org.apache.kafka.clients.consumer.{ConsumerConfig, ConsumerRecord}
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.spark.rdd.RDD
import org.apache.spark.streaming.kafka010.ConsumerStrategies.Subscribe
import org.apache.spark.streaming.kafka010.KafkaUtils
import org.apache.spark.streaming.kafka010.LocationStrategies.PreferConsistent

import scala.spores._

class Processor(config: ConfigLoader,
                sparkSessionProvider: SparkProvider) {

  private val kafkaParams = Map[String, Object](
    ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG -> config.kafkaBrokers,
    ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG -> classOf[StringDeserializer],
    ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG -> classOf[StringDeserializer],
    ConsumerConfig.GROUP_ID_CONFIG -> s"article-processor",
    ConsumerConfig.AUTO_OFFSET_RESET_CONFIG -> "latest",
    ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG -> (false: java.lang.Boolean))

  val mysqlUser = config.mysqlConfig.user
  val mysqlPassword = config.mysqlConfig.password
  val mysqlDriver = config.mysqlConfig.driver
  val mysqlUrl= config.mysqlConfig.jdbcUrl

  def apply = spore {

    val kafkaParams = this.kafkaParams
    val kafkaArticlesTopic = this.config.kafkaArticlesTopic

    val sparkProvider = sparkSessionProvider
    val recordToTuple = this.recordToTuple
    val saveDB = saveToDB(mysqlUser, mysqlPassword, mysqlDriver, mysqlUrl)
    val parseRecordsToArticles = this.parseRecordsToArticles

    () => {

      val logger = Logger(classOf[Processor])

      import org.apache.spark.streaming._

      val ssc = new StreamingContext(sparkProvider.getSparkConf(), Seconds(10))
      val stream = KafkaUtils.createDirectStream[String, String](
        ssc,
        PreferConsistent,
        Subscribe[String, String](Array(kafkaArticlesTopic), kafkaParams))

      stream
        .map(recordToTuple)
        .flatMap(parseRecordsToArticles)
        .foreachRDD(saveDB)

      ssc.start()
      ssc.awaitTermination()

      sys.addShutdownHook {
        logger.info("Stopping article streaming")
        try {
          ssc.stop(stopSparkContext = true, stopGracefully = true)
        } catch {
          case _: Throwable =>
        }
      }
    }
  }


  private[processor] def saveToDB(mysqlUser: String, mysqlPassword: String, mysqlDriver: String, mysqlUrl: String): (RDD[TwitterArticleRow]) => Unit = {

    rdd => {

      rdd.foreach { article =>

        val internLogger = Logger(classOf[Processor])

        val dbConfig = new java.util.Properties()
        dbConfig.put("user", mysqlUser)
        dbConfig.put("password", mysqlPassword)
        dbConfig.put("useSSL", "false")
        dbConfig.put("driver", mysqlDriver)

        val conn = DriverManager.getConnection(mysqlUrl, dbConfig)

        val query = conn.prepareStatement("" +
          "INSERT INTO article" +
          "(id, originalId, publicationDate, content, author, score) " +
          "VALUES (?,?,?,?,?,?)")

        query.setString(1, article.id)
        query.setString(2, article.originalId)
        query.setTimestamp(3, article.publicationDate)
        query.setString(4, article.content)
        query.setLong(5, article.author)
        query.setInt(6, article.score)

        try {
          query.executeUpdate
          query.close()
        } catch {
          case ex: SQLException =>
            internLogger.error(s"Failed to save article to DB. Article : $article", ex)
        }

        conn.close()

        internLogger.info(s"Article ${article.originalId} \n" +
          s"Score : ${article.score} \n" +
          s"Date : ${article.publicationDate} \n" +
          s"Content : ${article.content} \n")
      }
    }
  }

  private[processor] val parseRecordsToArticles: ((String, String)) => Iterable[TwitterArticleRow] = {
    case (_, value) => {
      TwitterArticleMarshaller.unmarshallArticle(value) match {
        case Some(article) => Some(new TwitterArticleRow(article))
        case None =>
          Logger(classOf[Processor]).error(s"Couldn't parse to an article : $value")
          Option.empty[TwitterArticleRow]
      }
    }
  }

  private[processor] val recordToTuple: (ConsumerRecord[String, String]) => (String, String) = {
    record => (record.key, record.value)
  }
}
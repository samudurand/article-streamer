package articlestreamer.processor

import java.sql.DriverManager
import java.util.UUID

import articlestreamer.processor.spark.SparkProvider
import articlestreamer.shared.configuration.ConfigLoader
import articlestreamer.shared.kafka.DualTopicManager
import articlestreamer.shared.marshalling.TwitterArticleMarshaller
import articlestreamer.shared.model.TwitterArticle
import articlestreamer.shared.model.db.TwitterArticleRow
import com.typesafe.scalalogging.{LazyLogging, Logger}
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.spark.streaming.kafka010.ConsumerStrategies.Subscribe
import org.apache.spark.streaming.kafka010.KafkaUtils
import org.apache.spark.streaming.kafka010.LocationStrategies.PreferConsistent

class Processor(config: ConfigLoader,
                sparkSessionProvider: SparkProvider,
                topicManager: DualTopicManager) {

  sys.addShutdownHook {
  }

  def apply(): Unit = {

    val logger = Logger(classOf[Processor])

    import org.apache.spark.streaming._

    val ssc = new StreamingContext(sparkSessionProvider.getSparkConf(), Seconds(10))

    val kafkaParams = Map[String, Object](
      ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG -> config.kafkaBrokers,
      ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG -> classOf[StringDeserializer],
      ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG -> classOf[StringDeserializer],
      ConsumerConfig.GROUP_ID_CONFIG -> s"article-processor-${UUID.randomUUID().toString}",
      ConsumerConfig.AUTO_OFFSET_RESET_CONFIG -> "earliest",
      ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG -> (false: java.lang.Boolean)
    )

    val stream = KafkaUtils.createDirectStream[String, String](
      ssc,
      PreferConsistent,
      Subscribe[String, String](Array(config.kafkaArticlesTopic), kafkaParams)
    )

    val articles = stream
      .map(record => (record.key, record.value))
      .flatMap {
        case (_, value) =>
          TwitterArticleMarshaller.unmarshallArticle(value) match {
            case Some(article) => Some(article)
            case None =>
              logger.error(s"Couldn't parse to an article : $value")
              Option.empty[TwitterArticle]
          }
      }
      .map(article => new TwitterArticleRow(article))

    articles.cache()

    articles.foreachRDD { rdd =>

      //Class.forName("com.mysql.jdbc.Driver")

      val conf = config

      rdd.foreach { article =>

        val internLogger = Logger(classOf[Processor])

        val dbConfig = new java.util.Properties()
        dbConfig.put("user", conf.mysqlConfig.user)
        dbConfig.put("password", conf.mysqlConfig.password)
        dbConfig.put("useSSL", "false")
        dbConfig.put("driver", "com.mysql.jdbc.Driver")

        val conn = DriverManager.getConnection(conf.mysqlConfig.jdbcUrl, dbConfig)

        val del = conn.prepareStatement("" +
          s"INSERT INTO article" +
          s"(id, originalId, publicationDate, content, author, score) " +
          s"VALUES (?,?,?,?,?,?)")

        del.setString(1, article.id)
        del.setString(2, article.originalId)
        del.setTimestamp(3, article.publicationDate)
        del.setString(4, article.content)
        del.setLong(5, article.author)
        del.setInt(6, article.score)

        del.executeUpdate

        conn.close()

        internLogger.info(s"Article ${article.originalId} \n" +
          s"Score : ${article.score} \n" +
          s"Date : ${article.publicationDate} \n" +
          s"Content : ${article.content} \n")
      }

    }

    ssc.start()
    ssc.awaitTermination()

    sys.addShutdownHook {
      ssc.stop(stopSparkContext = true, stopGracefully = true)
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
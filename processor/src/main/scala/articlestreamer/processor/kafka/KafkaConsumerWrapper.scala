package articlestreamer.processor.kafka

import java.util
import java.util.{Properties, UUID}

import articlestreamer.processor.marshalling.RecordMarshaller
import articlestreamer.shared.configuration.ConfigLoader
import articlestreamer.shared.kafka.KafkaFactory
import articlestreamer.shared.model.TwitterArticle
import com.typesafe.scalalogging.LazyLogging
import org.apache.kafka.clients.CommonClientConfigs
import org.apache.kafka.clients.consumer.{ConsumerConfig, ConsumerRecord, ConsumerRecords}
import org.apache.kafka.common.config.SslConfigs

import scala.collection.mutable
import scala.concurrent.duration._
import scala.language.postfixOps

/**
  * This class uses Java style loops to avoid using the JavaCollections methods provided by scala (using those would complicate the Unit tests)
  */
class KafkaConsumerWrapper(config: ConfigLoader, factory: KafkaFactory[String, String]) extends LazyLogging with RecordMarshaller {

  val topic = config.kafkaMainTopic
  val pollingTimeout = 1 seconds

  private val consumer = factory.getConsumer(KafkaConsumerWrapper.getProperties(config))

  consumer.subscribe(util.Arrays.asList(topic))

  /**
    * Pull all available messages from a kafka Topic. Stops once it finds an end-of-queue message or after several empty results.
    * The number of attempts before stopping is configured by
    * @return Records parsed as TwitterArticle
    */
  def pullAll(): List[TwitterArticle] = {

    logger.info("Polling started.")

    val millis = pollingTimeout.toMillis

    val articles = new mutable.ListBuffer[TwitterArticle]()
    var endFound = false
    var callAttempts = 0
    while (!endFound && callAttempts < config.kafkaMaxAttempts) {
      val records = consumer.poll(millis)

      val count = records.count()
      if (count > 0) {
        callAttempts = 0
        endFound = parseRecords(records, articles, count)
      } else {
        callAttempts += 1
      }
    }

    if (callAttempts == config.kafkaMaxAttempts) {
      logger.warn(s"Could not find the end of queue, polling stopped after ${config.kafkaMaxAttempts} attempts.")
    }

    logger.info("Polling Completed.")

    articles.toList
  }

  private def parseRecords(records: ConsumerRecords[String, String], articles: mutable.ListBuffer[TwitterArticle], count: Int): Boolean = {
    logger.info(s"Parsing $count records into articles")

    var endFound = false
    val recordsIterator: util.Iterator[ConsumerRecord[String, String]] = records.iterator()
    while (recordsIterator.hasNext) {

      val maybeRecord = unmarshallRecord(recordsIterator.next().value())
      if (maybeRecord.isEmpty) {

        logger.warn(s"Could not parse record $maybeRecord into an article.")

      } else {

        val record = maybeRecord.get
        if (record.endOfQueue) {
          endFound = true
        } else {
          val article = record.article.get
          articles += article
        }

      }
    }

    endFound
  }

  def stopConsumer() = {
    logger.info("Stopping consumer.")
    consumer.close()
  }

}

object KafkaConsumerWrapper {

  def getProperties(configLoader: ConfigLoader): Properties = {

    import configLoader._

    val properties = new Properties()
    properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaBrokers)
    properties.put(ConsumerConfig.GROUP_ID_CONFIG, "test-" + UUID.randomUUID().toString)
    properties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true")
    properties.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "1000")
    properties.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, "30000")
    properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer")
    properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer")
    properties.put(ConsumerConfig.FETCH_MAX_WAIT_MS_CONFIG, "5000")
    properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")

    if (kafkaSSLMode) {
      properties.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SSL")
      properties.put(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG, s"$kafkaTrustStore/truststore.jks")
      properties.put(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG, "test1234")
      properties.put(SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG, s"$kafkaTrustStore/keystore.jks")
      properties.put(SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG, "test1234")
      properties.put(SslConfigs.SSL_KEY_PASSWORD_CONFIG, "test1234")
    }

    properties
  }

}
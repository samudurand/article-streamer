package articlestreamer.processor.kafka

import java.util
import java.util.{Properties, UUID}

import articlestreamer.processor.marshalling.RecordMarshaller
import articlestreamer.shared.configuration.ConfigLoader
import articlestreamer.shared.kafka.KafkaFactory
import articlestreamer.shared.model.TwitterArticle
import articlestreamer.shared.model.kafka.ArticleRecord
import com.typesafe.scalalogging.LazyLogging
import org.apache.kafka.clients.CommonClientConfigs
import org.apache.kafka.clients.consumer.{ConsumerConfig, ConsumerRecord}
import org.apache.kafka.common.config.SslConfigs

import scala.collection.mutable
import scala.concurrent.duration.Duration

/**
  * This class uses Java style loops to avoid using the JavaCollections methods provided by scala (using those would complicate the Unit tests)
  */
class KafkaConsumerWrapper(config: ConfigLoader, factory: KafkaFactory[String, String]) extends LazyLogging with RecordMarshaller {

  val topic = config.kafkaMainTopic

  private val consumer = factory.getConsumer(KafkaConsumerWrapper.getProperties(config))

  consumer.subscribe(util.Arrays.asList(topic))

  def poll(duration: Duration, count: Int): List[TwitterArticle] = {

    logger.info("Polling started.")

    val millis = duration.toMillis

    val values = new mutable.ListBuffer[TwitterArticle]()
    var endFound = false
    var callAttempts = 0
    while (!endFound && callAttempts < config.kafkaMaxAttempts) {

      val records = consumer.poll(millis)

      if (records.count() > 0) {
        callAttempts = 0

        val recordsIterator: util.Iterator[ConsumerRecord[String, String]] = records.iterator()
        while (recordsIterator.hasNext) {

          val maybeRecord = unmarshallRecord(recordsIterator.next().value())
          if (maybeRecord.isEmpty) {

            println(s"Could not parse record $maybeRecord into an article.")

          } else {

            val record = maybeRecord.get
            if (record.endOfQueue) {
              endFound = true
            } else {
              val article = record.article.get
              values += article
            }

          }
        }
      } else {
        callAttempts += 1
      }
    }

    if (callAttempts == config.kafkaMaxAttempts) {
      logger.warn(s"Could not find the end of queue, polling stopped after ${config.kafkaMaxAttempts} attempts.")
    }

    logger.info("Polling Completed.")

    values.toList
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
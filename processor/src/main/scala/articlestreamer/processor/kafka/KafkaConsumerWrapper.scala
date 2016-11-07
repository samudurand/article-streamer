package articlestreamer.processor.kafka

import java.util
import java.util.{Properties, UUID}

import articlestreamer.shared.configuration.ConfigLoader
import articlestreamer.shared.kafka.KafkaConsumerFactory
import org.apache.kafka.clients.CommonClientConfigs
import org.apache.kafka.clients.consumer.{ConsumerConfig, ConsumerRecord}
import org.apache.kafka.common.config.SslConfigs

import scala.collection.mutable
import scala.concurrent.duration.Duration

class KafkaConsumerWrapper(config: ConfigLoader, factory: KafkaConsumerFactory[String, String]) {

  val topic = config.kafkaMainTopic

  private val consumer = factory.getConsumer(KafkaConsumerWrapper.getProperties(config))

  consumer.subscribe(util.Arrays.asList(topic))

  def poll(duration: Duration, count: Int): List[String] = {

    println("Polling started.")

    val millis = duration.toMillis

    val values = new mutable.ListBuffer[String]()
    for(i <- 1 to count) {
      val recordsIterator: util.Iterator[ConsumerRecord[String, String]] = consumer.poll(millis).iterator()
      while(recordsIterator.hasNext) {
        values += recordsIterator.next().value()
      }
    }

    println("Polling Completed.")

    values.toList
  }

//  /**
//   * Process the records and return (num processing successful, num processing failed)
//   */
//  type RecordHandler = Iterator[ConsumerRecord[String, AnyRef]] => (Int, Int)
//
//  private val consumer = new KafkaConsumer[String, AnyRef](KafkaConsumerWrapper.properties)
//
//  consumer.subscribe(util.Arrays.asList(KafkaConsumerWrapper.topic))
//
//  def poll(duration: Duration, count: Int, processing: RecordHandler): Unit = {
//    val millis = duration.toMillis
//
//    (1 to count).foreach { _ =>
//      val records = consumer.poll(millis)
//      val totalRecords = records.count()
//      println(s"retrieved ${} records")
//      Future {
//        processing(records.iterator())
//      } onComplete {
//        case Success(processingResults) => println(s"Finished to process $totalRecords. " +
//          s"Successes : ${processingResults._1}, failures: ${processingResults._2}.")
//        case Failure(ex: Exception) => System.err.println(ex)
//      }
//    }
//  }

  def stopConsumer() = {
    println("Stopping consumer.")
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
      properties.put(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG, s"${kafkaTrustStore}/truststore.jks")
      properties.put(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG, "test1234")
      properties.put(SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG, s"${kafkaTrustStore}/keystore.jks")
      properties.put(SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG, "test1234")
      properties.put(SslConfigs.SSL_KEY_PASSWORD_CONFIG, "test1234")
    }

    properties
  }

}
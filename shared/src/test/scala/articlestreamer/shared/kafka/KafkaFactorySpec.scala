package articlestreamer.shared.kafka

import java.util.Properties

import articlestreamer.shared.BaseSpec
import org.apache.kafka.clients.CommonClientConfigs
import org.apache.kafka.clients.consumer.{ConsumerConfig, KafkaConsumer}
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerConfig}

/**
  * Created by sam on 08/11/2016.
  */
class KafkaFactorySpec extends BaseSpec {

  val factory = new KafkaFactory[String, String]

  val serverProps = new Properties()
  serverProps.setProperty(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, "http://localhost:8080")
  serverProps.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer")
  serverProps.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer")

  val consumerProps = new Properties()
  consumerProps.setProperty(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, "http://localhost:8080")
  consumerProps.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer")
  consumerProps.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer")

  "Factory" should "provide a producer" in {
    val producer = factory.getProducer(serverProps)
    producer.close()
    producer shouldBe a [KafkaProducer[_, _]]
  }

  "Factory" should "provide a consumer" in {
    val consumer = factory.getConsumer(consumerProps)
    consumer.close()
    consumer shouldBe a [KafkaConsumer[_, _]]
  }

}

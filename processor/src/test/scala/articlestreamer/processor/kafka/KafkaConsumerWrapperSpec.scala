package articlestreamer.processor.kafka

import articlestreamer.shared.BaseSpec
import articlestreamer.shared.configuration.ConfigLoader
import articlestreamer.shared.kafka.KafkaConsumerFactory
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.mockito.ArgumentMatchers._
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfter

class KafkaConsumerWrapperSpec extends BaseSpec with BeforeAndAfter {

  class TestConfig extends ConfigLoader

  var consumer: KafkaConsumer[String, String] = _
  val factory = mock(classOf[KafkaConsumerFactory[String, String]])
  var consumerWrapper: KafkaConsumerWrapper = _

  before {
    consumer = mock(classOf[KafkaConsumer[String, String]])
    when(factory.getConsumer(any())).thenReturn(consumer)

    consumerWrapper = new KafkaConsumerWrapper(new TestConfig, factory)
  }

}

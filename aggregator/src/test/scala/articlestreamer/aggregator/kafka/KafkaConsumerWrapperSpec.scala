package articlestreamer.aggregator.kafka

import articlestreamer.shared.BaseSpec
import articlestreamer.shared.configuration.ConfigLoader
import articlestreamer.shared.kafka.KafkaConsumerFactory
import org.apache.kafka.clients.consumer.{ConsumerRecords, KafkaConsumer}
import org.mockito.ArgumentMatchers._
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfter

import scala.concurrent.duration._

/**
  * Created by sam on 03/11/2016.
  */
class KafkaConsumerWrapperSpec extends BaseSpec with BeforeAndAfter {

  class TestConfig extends ConfigLoader

  var consumer: KafkaConsumer[String, AnyRef] = _
  val factory = mock(classOf[KafkaConsumerFactory[String, AnyRef]])
  var consumerWrapper: KafkaConsumerWrapper = _

  before {
    consumer = mock(classOf[KafkaConsumer[String, AnyRef]])
    when(factory.getConsumer(any())).thenReturn(consumer)

    consumerWrapper = new KafkaConsumerWrapper(new TestConfig, factory)
  }

  "Wrapper" should "subscribe on init" in {
    verify(consumer, times(1)).subscribe(anyList())
  }

  it should "poll 10 times" in {
    val records = mock(classOf[ConsumerRecords[String, AnyRef]])
    when(consumer.poll(1000)).thenReturn(records)

    consumerWrapper.poll(1 second, 10)

    verify(consumer, times(10)).poll(1000)
  }

  it should "close the Consumer" in {
    consumerWrapper.stopConsumer()
    verify(consumer, times(1)).close()
  }

  "Config" should "be tested with SSL" in {
    class Test2Config extends ConfigLoader {
      override val kafkaSSLMode: Boolean = true
    }

    consumerWrapper = new KafkaConsumerWrapper(new Test2Config, factory)
  }

  "Config" should "be tested without SSL" in {
    class Test2Config extends ConfigLoader {
      override val kafkaSSLMode: Boolean = false
    }

    consumerWrapper = new KafkaConsumerWrapper(new Test2Config, factory)
  }

}
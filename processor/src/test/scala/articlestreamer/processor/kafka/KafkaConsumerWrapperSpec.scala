package articlestreamer.processor.kafka

import java.util

import articlestreamer.shared.BaseSpec
import articlestreamer.shared.configuration.ConfigLoader
import articlestreamer.shared.kafka.KafkaFactory
import org.apache.kafka.clients.consumer.{ConsumerRecord, ConsumerRecords, KafkaConsumer}
import org.apache.kafka.common.TopicPartition
import org.mockito.ArgumentMatchers._
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfter

import scala.concurrent.duration._

class KafkaConsumerWrapperSpec extends BaseSpec with BeforeAndAfter {

  class TestConfig extends ConfigLoader

  var consumer: KafkaConsumer[String, String] = _
  val factory = mock(classOf[KafkaFactory[String, String]])
  var consumerWrapper: KafkaConsumerWrapper = _

  before {
    consumer = mock(classOf[KafkaConsumer[String, String]])
    when(factory.getConsumer(any())).thenReturn(consumer)

    consumerWrapper = new KafkaConsumerWrapper(new TestConfig, factory)
  }

  "Wrapper" should "poll 3 times" in {
    val r1 = new ConsumerRecord[String, String]("topic", 1, 0, "key", "val")
    val r2 = new ConsumerRecord[String, String]("topic", 1, 0, "key2", "val2")
    prepareRecords(util.Arrays.asList(r1, r2))

    val values = consumerWrapper.poll(1 second, 3)

    verify(consumer, times(3)).poll(anyLong())
    values should have length(6)
    values shouldBe List(r1.value(), r2.value(), r1.value(), r2.value(), r1.value(), r2.value())
  }

  it should "poll 0 times" in {
    val r1 = new ConsumerRecord[String, String]("topic", 1, 0, "key", "val")
    prepareRecords(util.Arrays.asList(r1))

    val values = consumerWrapper.poll(1 second, 0)

    verify(consumer, never).poll(anyLong())
    values shouldBe empty
  }

  it should "stop consumer" in {
    consumerWrapper.stopConsumer()
    verify(consumer, times(1)).close()
  }

  private def prepareRecords(records: util.List[ConsumerRecord[String, String]]): Unit = {
    val mapRecords = new util.HashMap[TopicPartition, util.List[ConsumerRecord[String, String]]]()
    mapRecords.put(new TopicPartition("topic", 1), records)
    val consRecords = new ConsumerRecords[String, String](mapRecords)
    when(consumer.poll(anyLong())).thenReturn(consRecords)
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

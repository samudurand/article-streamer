

package articlestreamer.processor.kafka

import java.util

import articlestreamer.shared.BaseSpec
import articlestreamer.shared.configuration.ConfigLoader
import articlestreamer.shared.kafka.KafkaFactory
import articlestreamer.shared.marshalling.CustomJsonFormats
import org.apache.kafka.clients.consumer.{ConsumerRecord, ConsumerRecords, KafkaConsumer}
import org.apache.kafka.common.TopicPartition
import org.mockito.ArgumentMatchers._
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfter

import scala.io.Source
import scala.language.postfixOps

class KafkaConsumerWrapperSpec extends BaseSpec with BeforeAndAfter with CustomJsonFormats {

  class TestConfig extends ConfigLoader {
    override val kafkaMaxAttempts: Int = 3
  }

  var consumer: KafkaConsumer[String, String] = _
  val factory = mock(classOf[KafkaFactory[String, String]])
  var consumerWrapper: KafkaConsumerWrapper = _

  before {
    consumer = mock(classOf[KafkaConsumer[String, String]])
    when(factory.getConsumer(any())).thenReturn(consumer)

    consumerWrapper = new KafkaConsumerWrapper(new TestConfig, factory)
  }

  "Wrapper" should "pull all messages then stop on end of queue message" in {
    val tweet1 = Source.fromURL(getClass.getResource("/data/record-twitter-article.json")).mkString
    val tweet2 = Source.fromURL(getClass.getResource("/data/record-twitter-article-2.json")).mkString

    val r1 = new ConsumerRecord[String, String]("topic", 1, 0, "key", tweet1)
    val r2 = new ConsumerRecord[String, String]("topic", 1, 0, "key2", tweet2)
    prepareRecords(util.Arrays.asList(r1, r2), includeEndOfQueue = true)

    val values = consumerWrapper.pullAll()

    verify(consumer, times(2)).poll(anyLong())
    values should have length 2
    values(0).id shouldBe "00000000-0000-0000-0000-000000000001"
    values(1).id shouldBe "00000000-0000-0000-0000-000000000002"
  }

  it should "pull all messages then stop without finding end of queue" in {
    val tweet1 = Source.fromURL(getClass.getResource("/data/record-twitter-article.json")).mkString
    val tweet2 = Source.fromURL(getClass.getResource("/data/record-twitter-article-2.json")).mkString

    val r1 = new ConsumerRecord[String, String]("topic", 1, 0, "key", tweet1)
    val r2 = new ConsumerRecord[String, String]("topic", 1, 0, "key2", tweet2)
    prepareRecords(util.Arrays.asList(r1, r2), includeEndOfQueue = false)

    val values = consumerWrapper.pullAll()

    verify(consumer, times(4)).poll(anyLong())
    values should have length 2
    values(0).id shouldBe "00000000-0000-0000-0000-000000000001"
    values(1).id shouldBe "00000000-0000-0000-0000-000000000002"
  }

  it should "handle a badly formatted record" in {
    val badlyFormatted = Source.fromURL(getClass.getResource("/data/record-twitter-article-bad-formatting.json")).mkString
    val tweet1 = Source.fromURL(getClass.getResource("/data/record-twitter-article.json")).mkString

    val r0 = new ConsumerRecord[String, String]("topic", 1, 0, "key2", badlyFormatted)
    val r1 = new ConsumerRecord[String, String]("topic", 1, 0, "key", tweet1)
    prepareRecords(util.Arrays.asList(r0, r1), includeEndOfQueue = true)

    val values = consumerWrapper.pullAll()

    verify(consumer, times(2)).poll(anyLong())
    values should have length 1
    values(0).id shouldBe "00000000-0000-0000-0000-000000000001"
  }

  it should "stop consumer" in {
    consumerWrapper.stopConsumer()
    verify(consumer, times(1)).close()
  }

  private def prepareRecords(records: util.List[ConsumerRecord[String, String]], includeEndOfQueue: Boolean): Unit = {
    val mapRecords = new util.HashMap[TopicPartition, util.List[ConsumerRecord[String, String]]]()
    mapRecords.put(new TopicPartition("topic", 1), records)
    val consRecords = new ConsumerRecords[String, String](mapRecords)
    val noRecords = new ConsumerRecords[String, String](new util.HashMap())

    if (!includeEndOfQueue) {
      when(consumer.poll(anyLong()))
        .thenReturn(consRecords)
        .thenReturn(noRecords)
    } else {
      val endOfQueue = Source.fromURL(getClass.getResource("/data/record-end-of-queue.json")).mkString
      val endRecord = new ConsumerRecord[String, String]("topic", 1, 0, "key", endOfQueue)
      val mapEndRecord = new util.HashMap[TopicPartition, util.List[ConsumerRecord[String, String]]]()
      mapEndRecord.put(new TopicPartition("topic", 1), util.Arrays.asList(endRecord))
      val endRecords = new ConsumerRecords[String, String](mapEndRecord)

      when(consumer.poll(anyLong()))
        .thenReturn(consRecords)
        .thenReturn(endRecords)
    }
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




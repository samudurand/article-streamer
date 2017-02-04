package articlestreamer.shared.kafka

import articlestreamer.shared.BaseSpec
import articlestreamer.shared.configuration.ConfigLoader
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers._
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfter

class KafkaProducerWrapperSpec extends BaseSpec with BeforeAndAfter {

  class TestConfig extends ConfigLoader

  var producer: KafkaProducer[String, String] = _
  val factory = mock(classOf[KafkaFactory[String, String]])
  var producerWrapper: KafkaProducerWrapper = _

  before {
    producer = mock(classOf[KafkaProducer[String, String]])
    when(factory.getProducer(any())).thenReturn(producer)

    producerWrapper = new KafkaProducerWrapper(new TestConfig, factory)
  }

  "Wrapper" should "acquire producer" in {
    verify(factory, times(1)).getProducer(any())
  }

  it should "send record" in {
    val record = new ProducerRecord[String, String]("", "")
    producerWrapper.send(record)
    verify(producer, times(1)).send(ArgumentMatchers.eq(record), any())
  }

  it should "stop producer" in {
    producerWrapper.stopProducer()
    verify(producer, times(1)).close()
  }

//  "Config" should "be tested with SSL" in {
//    class Test2Config extends ConfigLoader {
//      override val kafkaSSLMode: Boolean = true
//    }
//
//    producerWrapper = new KafkaProducerWrapper(new Test2Config, factory)
//  }
//
//  it should "be tested without SSL" in {
//    class Test2Config extends ConfigLoader {
//      override val kafkaSSLMode: Boolean = false
//    }
//
//    producerWrapper = new KafkaProducerWrapper(new Test2Config, factory)
//  }

}

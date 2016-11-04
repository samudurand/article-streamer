package articlestreamer.aggregator.kafka

import articlestreamer.shared.BaseSpec
import org.apache.kafka.clients.producer.RecordMetadata
import org.apache.kafka.common.TopicPartition
import org.mockito.Mockito.mock

/**
  * Created by sam on 04/11/2016.
  */
class RecordCallbackSpec extends BaseSpec {

  val recordCallback = new RecordCallback

  //At the moment purely for coverage purpose
  "If any exception occurs" should "log it" in {
    recordCallback.onCompletion(null, new RuntimeException())
  }

  //At the moment purely for coverage purpose
  "If successfully sends record" should "log it" in {
    val metadata = new RecordMetadata(new TopicPartition("", 0), 0l, 1l)
    recordCallback.onCompletion(metadata, null)
  }

}

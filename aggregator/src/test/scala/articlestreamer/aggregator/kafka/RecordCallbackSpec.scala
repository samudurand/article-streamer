package articlestreamer.aggregator.kafka

import articlestreamer.shared.BaseSpec
import articlestreamer.shared.kafka.RecordCallback
import com.github.ghik.silencer.silent
import org.apache.kafka.clients.producer.RecordMetadata
import org.apache.kafka.common.TopicPartition

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
    //noinspection ScalaDeprecation
    val metadata = new RecordMetadata(new TopicPartition("", 0), 0l, 1l) : @silent
    recordCallback.onCompletion(metadata, null)
  }

}

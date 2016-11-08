package articlestreamer.aggregator.kafka

import org.apache.kafka.clients.producer.{Callback, RecordMetadata}

class RecordCallback extends Callback {

  override def onCompletion(metadata: RecordMetadata, ex: Exception) = {
    if (ex != null) {
      handleException(ex)
    } else {
      println(s"Successfully sent message : $metadata")
    }
  }
  
  private def handleException(exception: Exception): Unit = {
    Console.err.println(s"Error while attempting to send message : $exception")
  }
}

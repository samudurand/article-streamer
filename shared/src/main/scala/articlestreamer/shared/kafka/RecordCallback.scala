package articlestreamer.shared.kafka

import com.typesafe.scalalogging.LazyLogging
import org.apache.kafka.clients.producer.{Callback, RecordMetadata}

class RecordCallback extends Callback with LazyLogging {

  override def onCompletion(metadata: RecordMetadata, ex: Exception) = {
    if (ex != null) {
      handleException(ex)
    } else {
      logger.info(s"Successfully sent message : $metadata")
    }
  }
  
  private def handleException(exception: Exception): Unit = {
    logger.error("Error while attempting to send message", exception)
  }
}

package articlestreamer.processor.marshalling

import articlestreamer.shared.marshalling.CustomJsonFormats
import articlestreamer.shared.model.TwitterArticle
import articlestreamer.shared.model.kafka.KafkaRecord
import com.typesafe.scalalogging.LazyLogging
import org.json4s.jackson.Serialization._

object RecordMarshaller extends Serializable with CustomJsonFormats with LazyLogging {

  def unmarshallRecord(record: String): Option[TwitterArticle] = {

    try {
      Some(read[TwitterArticle](record))
    } catch {
      case ex: Throwable =>
        logger.error("Failed to parse article, exception thrown.", ex)
        None
    }
  }

}

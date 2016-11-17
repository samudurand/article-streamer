package articlestreamer.processor.marshalling

import articlestreamer.shared.marshalling.CustomJsonFormats
import articlestreamer.shared.model.kafka.KafkaRecord
import com.typesafe.scalalogging.LazyLogging
import org.json4s.jackson.Serialization._

trait RecordMarshaller extends Serializable with CustomJsonFormats with LazyLogging {

  def unmarshallRecord(record: String): Option[KafkaRecord] = {

    try {
      Some(read[KafkaRecord](record))
    } catch {
      case ex: Throwable =>
        logger.error(s"Failed to parse article, exception thrown. ${record.mkString}", ex)
        None
    }
  }

}
package articlestreamer.processor.marshalling

import articlestreamer.shared.exception.exceptions._
import articlestreamer.shared.marshalling.CustomJsonFormats
import articlestreamer.shared.model.TwitterArticle
import com.typesafe.scalalogging.LazyLogging
import org.json4s.jackson.Serialization._

object TwitterMarshaller extends Serializable with CustomJsonFormats with LazyLogging {

  def unmarshallTwitterArticle(articleJson: String): Option[TwitterArticle] = {

    try {
      Some(read[TwitterArticle](articleJson))
    } catch {
      case ex: Throwable =>
        logger.error("Failed to parse article, exception thrown.",ex)
        None
    }
  }

}

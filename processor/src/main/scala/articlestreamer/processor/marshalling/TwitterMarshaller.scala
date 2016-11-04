package articlestreamer.processor.marshalling

import articlestreamer.shared.exception.exceptions._
import articlestreamer.shared.marshalling.CustomJsonFormats
import articlestreamer.shared.model.TwitterArticle
import org.json4s.jackson.Serialization._

object TwitterMarshaller extends Serializable with CustomJsonFormats {

  def unmarshallTwitterArticle(articleJson: String): Option[TwitterArticle] = {

    try {
      Some(read[TwitterArticle](articleJson))
    } catch {
      case ex: Exception => {
        System.err.println(s"Failed to parse article, exception thrown. \n ${ex.getStackTraceAsString}")
        None
      }
    }
  }

}

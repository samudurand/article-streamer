package articlestreamer.processor.marshalling

import articlestreamer.shared.exception.exceptions._
import articlestreamer.shared.model.TwitterArticle

import org.json4s._
import org.json4s.jackson.Serialization
import org.json4s.jackson.Serialization.read

trait ArticleMarshaller {

  def unmarshallTwitterArticle(articleJson: String): Option[TwitterArticle] = {
    implicit val formats = Serialization.formats(NoTypeHints)

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

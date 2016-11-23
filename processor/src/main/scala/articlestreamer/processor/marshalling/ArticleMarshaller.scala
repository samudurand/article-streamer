package articlestreamer.processor.marshalling

import articlestreamer.shared.marshalling.CustomJsonFormats
import articlestreamer.shared.model.TwitterArticle
import com.typesafe.scalalogging.LazyLogging
import org.json4s.jackson.Serialization._

trait ArticleMarshaller extends Serializable with CustomJsonFormats with LazyLogging {

  def unmarshallArticle(article: String): Option[TwitterArticle] = {

    try {
      Some(read[TwitterArticle](article))
    } catch {
      case ex: Throwable =>
        logger.error(s"Failed to parse article, exception thrown. ${article.mkString}", ex)
        None
    }
  }

}


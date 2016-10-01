package articlestreamer.processor.marshalling

import articlestreamer.shared.exception.exceptions._
import articlestreamer.shared.model.{BaseArticle, TwitterArticle, Article}

import scala.pickling.Defaults._
import scala.pickling.json._

trait ArticleMarshaller {

  def unmarshallArticle(articleJson: String): Option[Article] = {
    try {
      articleJson.unpickle[Article] match {
        case twitter: TwitterArticle => Some(twitter)
        case basic: BaseArticle => Some(basic)
        case unknown => {
          System.err.println(s"Failed to parse article, found type [${unknown.getClass.getCanonicalName}]")
          None
        }
      }
    } catch {
      case ex: Exception => {
        System.err.println(s"Failed to parse article, exception thrown. \n ${ex.getStackTraceAsString}")
        None
      }
    }
  }

}

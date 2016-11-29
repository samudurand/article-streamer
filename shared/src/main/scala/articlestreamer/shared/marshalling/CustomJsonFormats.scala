package articlestreamer.shared.marshalling

import java.sql.Timestamp

import articlestreamer.shared.exception.exceptions._
import com.typesafe.scalalogging.LazyLogging
import org.json4s.JsonAST.{JInt, JLong}
import org.json4s.{CustomSerializer, NoTypeHints, native}

trait CustomJsonFormats extends LazyLogging {

  val dformat: String = "yyyy-MM-dd hh:mm:ss"

  case object TimestampSerializer extends CustomSerializer[java.sql.Timestamp](format => (
    {
      case JInt(l) =>
        try {
          new Timestamp(l.toLong)
        } catch {
          case ex: Throwable => {
            logger.error(s"Error while parsing date : ${ex.getStackTraceAsString}")
            null
          }
        }
      case JLong(l) =>
        try {
          new Timestamp(l)
        } catch {
          case ex: Throwable => {
            logger.error(s"Error while parsing date : ${ex.getStackTraceAsString}")
            null
          }
        }
      case _ => null
    },
    {
      case ts: Timestamp =>
        JLong(ts.getTime)
    }
    )
  )

  implicit val json4sFormats =  native.Serialization.formats(NoTypeHints) + TimestampSerializer

}

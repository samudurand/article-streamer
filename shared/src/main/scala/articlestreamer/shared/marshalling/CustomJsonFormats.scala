package articlestreamer.shared.marshalling

import java.sql.Date
import java.text.SimpleDateFormat
import java.util.TimeZone

import articlestreamer.shared.exception.exceptions._
import org.json4s.{CustomSerializer, NoTypeHints, native}
import org.json4s.JsonAST.{JNull, JString}

trait CustomJsonFormats {

  val df: SimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss")
  df.setTimeZone(TimeZone.getTimeZone("GMT"))

  case object DateSerializer extends CustomSerializer[java.sql.Date](format => (
    {
      case JString(s) => {
        try {
          new Date(df.parse(s).getTime)
        } catch {
          case ex: Throwable => {
            System.err.println(s"Error while parsing date : ${ex.getStackTraceAsString}")
            null
          }
        }
      }
      case JNull => null
    },
    {
      case d: Date => JString(df.format(d))
    }
    )
  )

  implicit val json4sFormats =  native.Serialization.formats(NoTypeHints) + DateSerializer

}

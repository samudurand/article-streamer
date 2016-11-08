package articlestreamer.shared.marshalling

import java.sql.Date

import articlestreamer.shared.BaseSpec
import org.json4s.jackson.Serialization._

class CustomJsonFormatsSpec extends BaseSpec with CustomJsonFormats {

  "Date serializer" should "parse string to date" in {
    val date = read[Date]("\"2016-10-20 11:45:28\"")
    date.getTime shouldBe 1476963928000l
  }

  "Date serializer" should "write date as string" in {
    val json = write(new Date(1476963928000l))
    json shouldBe "\"2016-10-20 11:45:28\""
  }

  "Date serializer" should "fail to parse string" in {
    val date = read[Date]("\"2016-10 11:45:28\"")
    date shouldBe null
  }

  "Date serializer" should "parse null value to null" in {
    val date = read[Date]("null")
    date shouldBe null
  }

}

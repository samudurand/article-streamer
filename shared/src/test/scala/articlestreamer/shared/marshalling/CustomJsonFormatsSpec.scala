package articlestreamer.shared.marshalling

import java.sql.Timestamp

import articlestreamer.shared.BaseSpec
import org.json4s.jackson.Serialization._

class CustomJsonFormatsSpec extends BaseSpec with CustomJsonFormats {

  "Timestamp serializer" should "parse int to date" in {
    val ts = read[Timestamp]("1476963928000")
    ts.getTime shouldBe 1476963928000l
  }

  "Timestamp serializer" should "write date as string" in {
    val json = write(new Timestamp(1476963928000l))
    json shouldBe "1476963928000"
  }

  "Timestamp serializer" should "parse null value to null" in {
    val date = read[Timestamp]("null")
    date shouldBe null
  }

}

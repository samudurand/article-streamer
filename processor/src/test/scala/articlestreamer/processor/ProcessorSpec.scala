package articlestreamer.processor

import java.io.File
import java.sql._

import articlestreamer.processor.spark.SparkProvider
import articlestreamer.shared.BaseSpec
import articlestreamer.shared.configuration.{ConfigLoader, MysqlConfig}
import articlestreamer.shared.marshalling.CustomJsonFormats
import articlestreamer.shared.model.db.TwitterArticleRow
import com.holdenkarau.spark.testing.StreamingSuiteBase
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfter

import scala.collection.mutable.ListBuffer

class MysqlTestConfig(url: String) extends ConfigLoader {
  override val mysqlConfig: MysqlConfig = MysqlConfig(url, "", "", "org.apache.derby.jdbc.EmbeddedDriver")
}

/**
  * Created by sam on 16/10/2016.
  */
class ProcessorSpec extends BaseSpec with StreamingSuiteBase with BeforeAndAfter with CustomJsonFormats {

  private val jdbcUrl = buildTempDerbyUrl()

  var ssProvider: SparkProvider = _
  var processor: Processor = _

  setupDB()

  before {
    ssProvider = mock(classOf[SparkProvider])
  }

  it should "save articles into the database" in {

    val article1 = TwitterArticleRow(
      "d0fa3a2a-74a9-49d5-8fac-b12f810f29b8",
      "808316754900484096",
      new Timestamp(1481552706000l),
      "#spark test https://t.co/WLUWq7roAm",
      1935423961,
      10
    )

    val article2 = TwitterArticleRow(
      "d0fa3a2a-74a9-1111-8fac-b12f810f29b8",
      "808316111900484096",
      new Timestamp(1411152706000l),
      "#spark fantastic ! https://t.co/WLUWq7roAm",
      1911123961,
      20)

    val processor = new Processor(new MysqlTestConfig(jdbcUrl), ssProvider)

    List(sc.parallelize(List(article1, article2)))
      .foreach(processor.saveToDB)

    val data = retrieveSavedData
    data should have length 2
    data(0) shouldBe ((article2.id, article2.originalId, article2.publicationDate.toString, article2.content, article2.author, article2.score))
    data(1) shouldBe ((article1.id, article1.originalId, article1.publicationDate.toString, article1.content, article1.author, article1.score))

  }

  private def retrieveSavedData = {
    val data = ListBuffer[(String, String, String, String, Long, Int)]()

    val connection: Connection = DriverManager.getConnection(jdbcUrl)
    val sta: Statement = connection.createStatement()
    val res = sta.executeQuery("""select * from article order by id""")
    while (res.next()) {
      data += ((res.getString(1), res.getString(2), res.getString(3), res.getString(4), res.getLong(5), res.getInt(6)))
    }
    sta.close()
    connection.close()

    data
  }

  private def buildTempDerbyUrl(): String = {
    val tempDir = com.holdenkarau.spark.testing.Utils.createTempDir("./")
    val filePath = new File(tempDir, "metastore").getCanonicalPath
    s"jdbc:derby:;databaseName=$filePath;create=true"
  }

  private def setupDB() = {
    val connection: Connection = DriverManager.getConnection(jdbcUrl)
    val sta: Statement = connection.createStatement()
    sta.executeUpdate(
      """CREATE TABLE article (
        |"ID" varchar(36) NOT NULL,
        |"ORIGINALID" varchar(255) NOT NULL,
        |"PUBLICATIONDATE" TIMESTAMP NOT NULL,
        |"CONTENT" varchar(255) NOT NULL,
        |"AUTHOR" bigint DEFAULT NULL,
        |"SCORE" int NOT NULL,
        |"STATUS" int NOT NULL DEFAULT 0,
        |PRIMARY KEY ("ID"))""".stripMargin)
    connection.commit()
    sta.close()
    connection.close()
  }

}

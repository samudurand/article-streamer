package articlestreamer.processor

import java.sql.Timestamp

import articlestreamer.processor.jdbc.ConnectionProvider
import articlestreamer.processor.spark.SparkProvider
import articlestreamer.shared.BaseSpec
import articlestreamer.shared.configuration.{ConfigLoader, MysqlConfig}
import articlestreamer.shared.marshalling.CustomJsonFormats
import articlestreamer.shared.model.db.TwitterArticleRow
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.hamcrest.Matchers.{any => _}
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfter

import scala.io.Source

/**
  * Created by sam on 16/10/2016.
  */
class ProcessorSpec extends BaseSpec with BeforeAndAfter with CustomJsonFormats {

  class TestConfig extends ConfigLoader {
    override val tweetsBatchSize: Int = 1
    override val kafkaMaxAttempts: Int = 2
    override val mysqlConfig: MysqlConfig = MysqlConfig("", "", "")
  }

  val config = new TestConfig
  var ssProvider: SparkProvider = _
  var connProvider: ConnectionProvider = _
  var processor: Processor = _

  before {
    ssProvider = mock(classOf[SparkProvider])
    connProvider = mock(classOf[ConnectionProvider])
    processor = new Processor(new TestConfig, ssProvider, connProvider)
  }

  "Processor" should "map a record to a tuple" in {
    val record = new ConsumerRecord[String, String]("topic", 1, 0, "key1", "value1")
    val tuple: Tuple2[String, String] = processor.recordToTuple(record)

    tuple._1.shouldBe("key1")
    tuple._2.shouldBe("value1")
  }

  it should "parse a record as tuple to an article" in {

    val tweet = Source.fromURL(getClass.getResource("/data/record-twitter-article.json")).mkString
    val recordTuple = ("key1", tweet)

    val article = processor.parseRecordsToArticles(recordTuple)

    article shouldBe List(TwitterArticleRow(
      "d0fa3a2a-74a9-49d5-8fac-b12f810f29b8",
      "808316754900484096",
      new Timestamp(1481552706000l),
      "#spark test https://t.co/WLUWq7roAm",
      1935423961,
      10
    ))
  }

}

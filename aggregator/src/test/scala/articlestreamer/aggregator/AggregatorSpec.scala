package articlestreamer.aggregator

import java.text.{DateFormat, SimpleDateFormat}
import java.util
import java.util.{Date, TimeZone}
import java.util.concurrent.Future

import articlestreamer.aggregator.kafka.KafkaProducerWrapper
import articlestreamer.aggregator.scoring.TwitterScoreCalculator
import articlestreamer.aggregator.twitter.{TwitterStreamer, TwitterStreamerFactory}
import articlestreamer.shared.{AdditionalMatchers, BaseSpec}
import articlestreamer.shared.configuration.ConfigLoader
import articlestreamer.shared.model.TwitterArticle
import org.apache.kafka.clients.producer.{ProducerRecord, RecordMetadata}
import org.joda.time.DateTimeZone
import org.mockito.ArgumentCaptor
import org.scalamock.matchers.MatchAny
import org.scalamock.scalatest.MockFactory
import org.scalatest.Matchers
import twitter4j.{Status, URLEntity}
import org.json4s._
import org.json4s.jackson.Serialization
import org.json4s.jackson.Serialization.read

class AggregatorSpec extends BaseSpec with MockFactory with AdditionalMatchers {

  val df: DateFormat = new SimpleDateFormat("dd-MM-yyyy")
  df.setTimeZone(TimeZone.getDefault)

  class TestConfig extends ConfigLoader

  val config = new TestConfig()

  class TestProducer extends KafkaProducerWrapper(config) {

    val sendMock = mockFunction[ProducerRecord[String, String], Future[RecordMetadata]]

    override def send(record: ProducerRecord[String, String]): Future[RecordMetadata] = {
      sendMock(record)
    }

    override def stopProducer(): Unit = {}
  }

  class TestStreamer extends TwitterStreamer {
    val startStreamingMock = mockFunction[Unit]

    override def startStreaming(): Unit = startStreamingMock()

    override def stop(): Unit = mockFunction[Unit]
  }

  val kafkaWrapper = mock[TestProducer]

  val scoreCalculator = mock[TwitterScoreCalculator]
  (scoreCalculator.calculateBaseScore _).expects(*).returns(10)

  val streamer = new TestStreamer

  "Aggregator when started" should "begin streaming" in {
    val factory = mock[TwitterStreamerFactory]
    (factory.getStreamer(_: ConfigLoader, _: (Status) => Unit, _: () => Unit)).expects(*, *, *).returns(streamer)

    streamer.startStreamingMock expects() once()

    val aggregator = new Aggregator(config, kafkaWrapper, scoreCalculator, factory)
    aggregator.run()
  }

  "Every received tweet" should "be converted to an article and send to kafka" in {
    val tweetHandler = captureTweetHandler[(Status) => Unit]()

    val status = mock[Status]
    val date = df.parse("01-01-2000")
    (status.getCreatedAt _).expects().returns(date) twice()
    (status.getURLEntities _).expects().returns(List[URLEntity]().toArray)
    (status.getId _).expects().returns(1000l) twice()
    (status.getText _).expects().returns("some content")

    val captor = new ArgumentCaptor[ProducerRecord[String, String]]
    (kafkaWrapper.send _).expects(capture(captor))

    tweetHandler(status)

    implicit val formats = Serialization.formats(NoTypeHints)
    val recordSent = read[TwitterArticle](captor.valueCaptured.get.value())
    recordSent.content shouldBe "some content"
    recordSent.originalId shouldBe "1000"
    recordSent.score shouldBe Some(10)
  }

  def captureTweetHandler[T](): T = {
    val captor = new ArgumentCaptor[T]
    val factory = mock[TwitterStreamerFactory]
    (factory.getStreamer(_: ConfigLoader, _: (Status) => Unit, _: () => Unit)).expects(*, capture(captor), *).returns(streamer)

    streamer.startStreamingMock expects() once()

    val aggregator = new Aggregator(config, kafkaWrapper, scoreCalculator, factory)
    aggregator.run()
    captor.valueCaptured.get
  }

}

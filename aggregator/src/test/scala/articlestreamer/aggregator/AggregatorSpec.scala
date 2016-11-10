package articlestreamer.aggregator

import java.text.SimpleDateFormat
import java.util.TimeZone

import articlestreamer.aggregator.kafka.KafkaProducerWrapper
import articlestreamer.aggregator.twitter.{DefaultTwitterStreamerFactory, TwitterStreamer}
import articlestreamer.shared.BaseSpec
import articlestreamer.shared.configuration.ConfigLoader
import articlestreamer.shared.marshalling.CustomJsonFormats
import articlestreamer.shared.model.TwitterArticle
import articlestreamer.shared.scoring.TwitterScoreCalculator
import org.apache.kafka.clients.producer.ProducerRecord
import org.json4s.jackson.Serialization.read
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers._
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfter
import twitter4j.{Status, URLEntity, User}

class AggregatorSpec extends BaseSpec with BeforeAndAfter with CustomJsonFormats {

  val df: SimpleDateFormat = new SimpleDateFormat(dformat)
  df.setTimeZone(TimeZone.getTimeZone("GMT"))

  class TestConfig extends ConfigLoader

  val config = new TestConfig()

  var kafkaWrapper : KafkaProducerWrapper = _
  var scoreCalculator: TwitterScoreCalculator = _
  var streamer: TwitterStreamer = _

  before {
    kafkaWrapper = mock(classOf[KafkaProducerWrapper])
    scoreCalculator = mock(classOf[TwitterScoreCalculator])
    streamer = mock(classOf[TwitterStreamer])
  }

  "Aggregator when started" should "begin streaming" in {
    val factory = mock(classOf[DefaultTwitterStreamerFactory])
    when(factory.getStreamer(any(), any(), any())).thenReturn(streamer)

    val aggregator = new Aggregator(config, kafkaWrapper, scoreCalculator, factory)
    aggregator.run()

    verify(streamer, times(1)).startStreaming()
  }

  "Tweet received" should "be converted to an article and send to kafka" in {
    when(scoreCalculator.calculateBaseScore(any())).thenReturn(10)

    val uRLEntity = mock(classOf[URLEntity])
    when(uRLEntity.getExpandedURL).thenReturn("http://anyurl.com")

    val tweetHandler = captureTweetHandler()

    val status = mock(classOf[Status])
    val date = df.parse("01-01-2000 00:00:00")
    when(status.getCreatedAt).thenReturn(date)
    when(status.getURLEntities).thenReturn(List[URLEntity](uRLEntity).toArray)
    when(status.getId).thenReturn(1000l)
    when(status.getText).thenReturn("some content")
    val user = mock(classOf[User])
    when(status.getUser).thenReturn(user)

    val captor: ArgumentCaptor[ProducerRecord[String, String]]  = ArgumentCaptor.forClass(classOf[ProducerRecord[String, String]])
    when(kafkaWrapper.send(captor.capture())).thenReturn(null)

    tweetHandler(status)

    val recordSent: TwitterArticle = read[TwitterArticle](captor.getValue.value())
    recordSent.content shouldBe "some content"
    recordSent.originalId shouldBe "1000"
    recordSent.score shouldBe Some(10)
    recordSent.links shouldBe List("http://anyurl.com")
  }

  "Any tweet not potentially an article " should "be ignored" in {

    val tweetHandler = captureTweetHandler()

    val status = mock(classOf[Status])
    val date = df.parse("01-01-2000 00:00:00")
    when(status.getCreatedAt).thenReturn(date)
    when(status.getURLEntities).thenReturn(Array[URLEntity]())
    when(status.getId).thenReturn(1000l)
    when(status.getText).thenReturn("some content")
    val user = mock(classOf[User])
    when(status.getUser).thenReturn(user)

    val captor: ArgumentCaptor[ProducerRecord[String, String]]  = ArgumentCaptor.forClass(classOf[ProducerRecord[String, String]])
    when(kafkaWrapper.send(captor.capture())).thenReturn(null)

    tweetHandler(status)

    verify(kafkaWrapper, never()).send(any())
  }

  "All retweets" should "be ignored" in {
    val uRLEntity = mock(classOf[URLEntity])
    when(uRLEntity.getExpandedURL).thenReturn("http://anyurl.com")

    val tweetHandler = captureTweetHandler()

    val status = mock(classOf[Status])
    val date = df.parse("01-01-2000 00:00:00")
    when(status.getCreatedAt).thenReturn(date)
    when(status.getURLEntities).thenReturn(Array[URLEntity](uRLEntity))
    when(status.getId).thenReturn(1000l)
    when(status.getText).thenReturn("some content")
    when(status.isRetweet).thenReturn(true)
    val user = mock(classOf[User])
    when(status.getUser).thenReturn(user)

    val captor: ArgumentCaptor[ProducerRecord[String, String]]  = ArgumentCaptor.forClass(classOf[ProducerRecord[String, String]])
    when(kafkaWrapper.send(captor.capture())).thenReturn(null)

    tweetHandler(status)

    verify(kafkaWrapper, never()).send(any())
  }

  def captureTweetHandler(): (Status) => Unit = {
    val captor = ArgumentCaptor.forClass(classOf[(Status) => Unit])
    val factory = mock(classOf[DefaultTwitterStreamerFactory])
    when(factory.getStreamer(any(), captor.capture(), any())).thenReturn(streamer)

    val aggregator = new Aggregator(config, kafkaWrapper, scoreCalculator, factory)
    aggregator.run()
    captor.getValue()
  }

}

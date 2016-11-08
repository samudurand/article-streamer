package articlestreamer.aggregator

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
import twitter4j.{Status, URLEntity}

class AggregatorSpec extends BaseSpec with CustomJsonFormats {

  class TestConfig extends ConfigLoader

  val config = new TestConfig()

  val kafkaWrapper = mock(classOf[KafkaProducerWrapper])
  val scoreCalculator = mock(classOf[TwitterScoreCalculator])
  val streamer = mock(classOf[TwitterStreamer])

  "Aggregator when started" should "begin streaming" in {
    val factory = mock(classOf[DefaultTwitterStreamerFactory])
    when(factory.getStreamer(any(), any(), any())).thenReturn(streamer)

    val aggregator = new Aggregator(config, kafkaWrapper, scoreCalculator, factory)
    aggregator.run()

    verify(streamer, times(1)).startStreaming()
  }

  "Every received tweet" should "be converted to an article and send to kafka" in {
    when(scoreCalculator.calculateBaseScore(any())).thenReturn(10)

    val uRLEntity = mock(classOf[URLEntity])
    when(uRLEntity.getURL).thenReturn("http://anyurl.com")

    val tweetHandler = captureTweetHandler()

    val status = mock(classOf[Status])
    val date = df.parse("01-01-2000 00:00:00")
    when(status.getCreatedAt).thenReturn(date)
    when(status.getURLEntities).thenReturn(List[URLEntity](uRLEntity).toArray)
    when(status.getId).thenReturn(1000l)
    when(status.getText).thenReturn("some content")

    val captor: ArgumentCaptor[ProducerRecord[String, String]]  = ArgumentCaptor.forClass(classOf[ProducerRecord[String, String]])
    when(kafkaWrapper.send(captor.capture())).thenReturn(null)

    tweetHandler(status)

    val recordSent: TwitterArticle = read[TwitterArticle](captor.getValue.value())
    recordSent.content shouldBe "some content"
    recordSent.originalId shouldBe "1000"
    recordSent.score shouldBe Some(10)
    recordSent.links shouldBe List("http://anyurl.com")
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

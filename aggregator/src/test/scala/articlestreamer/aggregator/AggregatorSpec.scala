package articlestreamer.aggregator

import java.util.concurrent.Future

import articlestreamer.aggregator.kafka.KafkaProducerWrapper
import articlestreamer.aggregator.scoring.TwitterScoreCalculator
import articlestreamer.aggregator.twitter.{TwitterStreamer, TwitterStreamerFactory}
import articlestreamer.shared.BaseSpec
import articlestreamer.shared.configuration.ConfigLoader
import org.apache.kafka.clients.producer.{ProducerRecord, RecordMetadata}
import org.scalamock.scalatest.MockFactory
import twitter4j.Status

class AggregatorSpec extends BaseSpec with MockFactory {

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

  val streamer = new TestStreamer
  val factory = mock[TwitterStreamerFactory]
  (factory.getStreamer(_: ConfigLoader, _: (Status) => Unit, _: () => Unit)).expects(*, *, *).returns(streamer)

  "Aggregator when started" should "begin streaming" in {
    streamer.startStreamingMock expects() once()

    val aggregator = new Aggregator(config, kafkaWrapper, scoreCalculator, factory)
    aggregator.run()
  }

}

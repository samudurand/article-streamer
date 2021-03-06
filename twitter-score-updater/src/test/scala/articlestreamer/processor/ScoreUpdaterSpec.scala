package articlestreamer.processor

import java.sql.Timestamp
import java.util
import java.util.Arrays.asList
import java.util.concurrent

import articlestreamer.processor.kafka.KafkaConsumerWrapperSpec.prepareRecords
import articlestreamer.shared.BaseSpec
import articlestreamer.shared.configuration.{ConfigLoader, TwitterConfig}
import articlestreamer.shared.kafka.{DualTopicManager, KafkaFactory}
import articlestreamer.shared.marshalling.CustomJsonFormats
import articlestreamer.shared.model.{TweetAuthor, TwitterArticle}
import articlestreamer.shared.scoring.{NaiveTwitterScoreCalculator, TwitterScoreCalculator}
import articlestreamer.twitterupdater.ScoreUpdater
import org.apache.kafka.clients.consumer.{ConsumerRecord, ConsumerRecords, KafkaConsumer}
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord, RecordMetadata}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers._
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfter
import twitter4j.{Twitter, TwitterFactory}

import scala.collection.mutable
import scala.io.Source

/**
  * Created by sam on 16/10/2016.
  */
class ScoreUpdaterSpec extends BaseSpec with BeforeAndAfter with CustomJsonFormats {

  class TestConfig extends ConfigLoader {
    val tconfig = TwitterConfig(null, null, 1, null, null)
    override val twitterConfig: TwitterConfig = tconfig
    override val kafkaMaxAttempts: Int = 2
  }

  class TestTopicManager extends DualTopicManager {

    var currentTopic: String = getSecondTopic()

    override def getTopicList(): Array[String] = ???

    override def getCurrentTopic(): String = currentTopic

    override def getFirstTopic(): String = "topic1"

    override def getSecondTopic(): String = "topic2"

    override def getNotCurrentTopic(): String = ???
  }

  val config = new TestConfig

  var factory: TwitterFactory = _
  var consumerFactory: KafkaFactory[String, String] = _
  var consumer1: KafkaConsumer[String, String] = _
  var consumer2: KafkaConsumer[String, String] = _
  var producer: KafkaProducer[String, String] = _
  var scoreCalculator: TwitterScoreCalculator = _

  before {
    factory = mock(classOf[TwitterFactory])
    when(factory.getInstance).thenReturn(mock(classOf[Twitter]))

    scoreCalculator = mock(classOf[NaiveTwitterScoreCalculator])

    consumer1 = mock(classOf[KafkaConsumer[String, String]])
    consumer2 = mock(classOf[KafkaConsumer[String, String]])
    producer = mock(classOf[KafkaProducer[String, String]])
    consumerFactory = mock(classOf[KafkaFactory[String, String]])
    when(consumerFactory.getConsumer(any())).thenReturn(consumer1, consumer2)
    when(consumerFactory.getProducer(any())).thenReturn(producer)
  }

  it should "retrieve articles and update scores" in {

    prepareConsumersToPullTwoRecords(consumer1)

    val article = TwitterArticle("00000000-0000-0000-0000-000000000001", "789070025009336320", new Timestamp(123456789l),
      mutable.WrappedArray.empty, "", TweetAuthor(1234, "user1", 0), Some(10))
    val article2 = TwitterArticle("00000000-0000-0000-0000-000000000002", "789070025044436320", new Timestamp(123456789l),
      mutable.WrappedArray.empty, "", TweetAuthor(5678, "user2", 0), Some(20))

    val mapCaptor: ArgumentCaptor[Map[Long, TwitterArticle]] = ArgumentCaptor.forClass(classOf[Map[Long, TwitterArticle]])
    when(scoreCalculator.updateScores(mapCaptor.capture()))
      .thenReturn(List(article), List(article2))

    val recordsCaptor: ArgumentCaptor[ProducerRecord[String, String]] = ArgumentCaptor.forClass(classOf[ProducerRecord[String, String]])
    when(producer.send(recordsCaptor.capture(), any())).thenReturn(mock(classOf[concurrent.Future[RecordMetadata]]))

    val scoreUpdater = new ScoreUpdater(config, consumerFactory, scoreCalculator, new TestTopicManager)
    val articles = scoreUpdater()

    articles should have length 2
    articles(0).id shouldBe article.id
    articles(1).id shouldBe article2.id

    val processedArticles = mapCaptor.getAllValues
    processedArticles.get(0) should have size 1
    processedArticles.get(0)(789070025009336320l).id shouldBe article.id
    processedArticles.get(1) should have size 1
    processedArticles.get(1)(789070025044436320l).id shouldBe article2.id

    val sentRecords = recordsCaptor.getAllValues
    sentRecords.get(0).key() shouldBe s"tweet-${article.id}"
    sentRecords.get(1).key() shouldBe s"tweet-${article2.id}"
  }

  def prepareConsumersToPullTwoRecords(consumer: KafkaConsumer[String, String]) = {
    val tweet1 = Source.fromURL(getClass.getResource("/data/record-twitter-article.json")).mkString
    val tweet2 = Source.fromURL(getClass.getResource("/data/record-twitter-article-2.json")).mkString

    val r1 = new ConsumerRecord[String, String]("topic", 1, 0, "key", tweet1)
    val r2 = new ConsumerRecord[String, String]("topic", 1, 0, "key2", tweet2)
    // Whatever the time of day both consumer will return the same
    prepareRecords(consumer, asList(r1, r2), includeEndOfQueue = true)
  }

  it should "fail to retrieve scores" in {

    prepareConsumersToPullTwoRecords(consumer1)

    val article = TwitterArticle("00000000-0000-0000-0000-000000000001", "789070025009336320", new Timestamp(123456789l),
      List(), "", TweetAuthor(1234, "user1", 0), Some(20))

    when(scoreCalculator.updateScores(any()))
      .thenReturn(List(article))
      .thenThrow(new RuntimeException())

    val processor = new ScoreUpdater(config, consumerFactory, scoreCalculator, new TestTopicManager)
    val articles = processor()

    articles should have size 1
    articles.head.id shouldBe article.id
  }

  it should "process an empty queue" in {
    when(consumer1.poll(any())).thenReturn(new ConsumerRecords[String, String](new util.HashMap()))

    val processor = new ScoreUpdater(config, consumerFactory, scoreCalculator, new TestTopicManager)
    val articles = processor()

    articles shouldBe empty
    verify(scoreCalculator, never()).updateScores(any())
    verify(consumer1, times(2)).poll(any())
  }

  it should "poll from the right topic" in {
    val topicManager = new TestTopicManager
    topicManager.currentTopic = topicManager.getSecondTopic()

    when(consumer1.poll(any())).thenReturn(new ConsumerRecords[String, String](new util.HashMap()))
    when(consumer2.poll(any())).thenReturn(new ConsumerRecords[String, String](new util.HashMap()))

    val processor = new ScoreUpdater(config, consumerFactory, scoreCalculator, topicManager)
    processor()

    verify(consumer1, times(2)).poll(any())

    topicManager.currentTopic = topicManager.getFirstTopic()
    processor()

    verify(consumer2, times(2)).poll(any())
  }

  def extractArticles[T](captor: ArgumentCaptor[Map[Long, T]]): Iterable[T] = {
    val m = captor.getValue
    m.values
  }

}

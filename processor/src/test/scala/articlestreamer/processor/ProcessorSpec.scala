package articlestreamer.processor

import java.sql.Date
import java.util
import java.util.Arrays.asList

import articlestreamer.processor.kafka.KafkaConsumerWrapperSpec.prepareRecords
import articlestreamer.processor.spark.SparkSessionProvider
import articlestreamer.shared.BaseSpec
import articlestreamer.shared.configuration.ConfigLoader
import articlestreamer.shared.kafka.{DualTopicManager, KafkaFactory}
import articlestreamer.shared.marshalling.CustomJsonFormats
import articlestreamer.shared.model.{TweetAuthor, TwitterArticle}
import articlestreamer.shared.scoring.{NaiveTwitterScoreCalculator, TwitterScoreCalculator}
import com.holdenkarau.spark.testing.{DataFrameSuiteBase, SharedSparkContext}
import org.apache.kafka.clients.consumer.{ConsumerRecord, ConsumerRecords, KafkaConsumer}
import org.hamcrest.Matchers.{any => _}
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
class ProcessorSpec extends BaseSpec with SharedSparkContext with DataFrameSuiteBase with BeforeAndAfter with CustomJsonFormats {

  class TestConfig extends ConfigLoader {
    override val tweetsBatchSize: Int = 1
  }

  class TestTopicManager extends DualTopicManager {

    var currentTopic = getFirstTopic()

    override def getTopicList(): Array[String] = ???

    override def getCurrentTopic(): String = currentTopic

    override def getFirstTopic(): String = "topic1"

    override def getSecondTopic(): String = "topic2"
  }

  val config = new TestConfig

  var factory: TwitterFactory = _
  var ssProvider: SparkSessionProvider = _
  var consumerFactory: KafkaFactory[String, String] = _
  var consumer1: KafkaConsumer[String, String] = _
  var consumer2: KafkaConsumer[String, String] = _
  var scoreCalculator: TwitterScoreCalculator = _

  before {
    factory = mock(classOf[TwitterFactory])
    when(factory.getInstance).thenReturn(mock(classOf[Twitter]))

    ssProvider = mock(classOf[SparkSessionProvider])
    when(ssProvider.getSparkSession()).thenReturn(spark)

    scoreCalculator = mock(classOf[NaiveTwitterScoreCalculator])

    consumer1 = mock(classOf[KafkaConsumer[String, String]])
    consumer2 = mock(classOf[KafkaConsumer[String, String]])
    consumerFactory = mock(classOf[KafkaFactory[String, String]])
    when(consumerFactory.getConsumer(any())).thenReturn(consumer1, consumer2)
  }

  it should "retrieve articles and update scores, then return in proper order" in {

    prepareConsumersToPullTwoRecords(consumer1)

    val article = TwitterArticle("00000000-0000-0000-0000-000000000001", "789070025009336320", new Date(123456789l),
      mutable.WrappedArray.empty, "", TweetAuthor(1234, "user1", 0), Some(20))
    val article2 = TwitterArticle("00000000-0000-0000-0000-000000000002", "789070025044436320", new Date(123456789l),
      mutable.WrappedArray.empty, "", TweetAuthor(5678, "user2", 0), Some(10))

    val mapCaptor: ArgumentCaptor[Map[Long, TwitterArticle]] = ArgumentCaptor.forClass(classOf[Map[Long, TwitterArticle]])
    when(scoreCalculator.updateScores(mapCaptor.capture()))
      .thenReturn(List(article, article2), List())

    val processor = new Processor(config, consumerFactory, scoreCalculator, ssProvider, new TestTopicManager)
    val articles = processor.run()

    articles should have length 2
    articles(0).id shouldBe article2.id
    articles(1).id shouldBe article.id

    val processedArticles = mapCaptor.getAllValues
    processedArticles.get(0) should have size 1
    processedArticles.get(0)(789070025009336320l).id shouldBe article.id
    processedArticles.get(1) should have size 1
    processedArticles.get(1)(789070025044436320l).id shouldBe article2.id
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

    val article = TwitterArticle("00000000-0000-0000-0000-000000000001", "789070025009336320", new Date(123456789l),
      List(), "", TweetAuthor(1234, "user1", 0), Some(20))

    when(scoreCalculator.updateScores(any()))
      .thenReturn(List(article))
      .thenThrow(new RuntimeException())

    val processor = new Processor(config, consumerFactory, scoreCalculator, ssProvider, new TestTopicManager)
    val articles = processor.run()

    articles should have size 1
    articles.head.id shouldBe article.id
  }

  it should "process an empty queue" in {
    when(consumer1.poll(any())).thenReturn(new ConsumerRecords[String, String](new util.HashMap()))

    val processor = new Processor(config, consumerFactory, scoreCalculator, ssProvider, new TestTopicManager)
    val articles = processor.run()

    articles shouldBe empty
    verify(scoreCalculator, never()).updateScores(any())
    verify(consumer1, times(10)).poll(any())
  }

  it should "poll from the right topic" in {
    val topicManager = new TestTopicManager
    topicManager.currentTopic = topicManager.getFirstTopic()

    when(consumer1.poll(any())).thenReturn(new ConsumerRecords[String, String](new util.HashMap()))
    when(consumer2.poll(any())).thenReturn(new ConsumerRecords[String, String](new util.HashMap()))

    val processor = new Processor(config, consumerFactory, scoreCalculator, ssProvider, topicManager)
    processor.run()

    verify(consumer1, times(10)).poll(any())

    topicManager.currentTopic = topicManager.getSecondTopic()
    processor.run()

    verify(consumer2, times(10)).poll(any())
  }

  def extractArticles[T](captor: ArgumentCaptor[Map[Long, T]]): Iterable[T] = {
    val m = captor.getValue
    m.values
  }
}

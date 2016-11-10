package articlestreamer.processor

import java.sql.Date

import articlestreamer.processor.kafka.KafkaConsumerWrapper
import articlestreamer.processor.spark.SparkSessionProvider
import articlestreamer.shared.BaseSpec
import articlestreamer.shared.configuration.ConfigLoader
import articlestreamer.shared.model.{TweetAuthor, TwitterArticle}
import articlestreamer.shared.scoring.{NaiveTwitterScoreCalculator, TwitterScoreCalculator}
import com.holdenkarau.spark.testing.{DataFrameSuiteBase, SharedSparkContext}
import org.hamcrest.Matchers.{any => _}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers._
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfter
import twitter4j.{Twitter, TwitterFactory}

import scala.io.Source

/**
  * Created by sam on 16/10/2016.
  */
class ArticleProcessorSpec extends BaseSpec with SharedSparkContext with DataFrameSuiteBase  with BeforeAndAfter {

  class TestConfig extends ConfigLoader
  val config = new TestConfig

  var factory: TwitterFactory = _
  var ssProvider: SparkSessionProvider = _
  var consumer: KafkaConsumerWrapper = _
  var scoreCalculator: TwitterScoreCalculator = _

  before {
    factory = mock(classOf[TwitterFactory])
    when(factory.getInstance).thenReturn(mock(classOf[Twitter]))

    ssProvider = mock(classOf[SparkSessionProvider])
    when(ssProvider.getSparkSession()).thenReturn(spark)

    scoreCalculator = mock(classOf[NaiveTwitterScoreCalculator])
    consumer = mock(classOf[KafkaConsumerWrapper])
  }

  it should "retrieve articles and update scores" in {
    val tweet1 = Source.fromURL(getClass.getResource("/data/twitter-article.json")).mkString
    val tweet2 = Source.fromURL(getClass.getResource("/data/twitter-article-2.json")).mkString
    when(consumer.poll(any(), any())).thenReturn(List(tweet1, tweet2))

    val article = TwitterArticle("00000000-0000-0000-0000-000000000001", "789070025009336320", new Date(123456789l),
      List(), "", TweetAuthor(1234, "user1", 0), Some(20))
    val article2 = TwitterArticle("00000000-0000-0000-0000-000000000002", "789070025044436320", new Date(123456789l),
      List(), "", TweetAuthor(5678, "user2", 0), Some(10))
    val mapCaptor: ArgumentCaptor[Map[Long, TwitterArticle]] = ArgumentCaptor.forClass(classOf[Map[Long, TwitterArticle]])
    when(scoreCalculator.updateScores(mapCaptor.capture())).thenReturn(List(article, article2))

    val processor = new ArticleProcessor(config, consumer, scoreCalculator, ssProvider)
    val articles = processor.run()

    articles should have length 2
    articles shouldBe List(article2, article)

    val processedArticles = mapCaptor.getValue
    processedArticles should have size 2
    processedArticles(789070025009336320l).id shouldBe "00000000-0000-0000-0000-000000000001"
    processedArticles(789070025044436320l).id shouldBe "00000000-0000-0000-0000-000000000002"
  }

  it should "fail to retrieve any scores" in {
    val tweet1 = Source.fromURL(getClass.getResource("/data/twitter-article.json")).mkString
    when(consumer.poll(any(), any())).thenReturn(List(tweet1))

    when(scoreCalculator.updateScores(any())).thenThrow(new RuntimeException())

    val processor = new ArticleProcessor(config, consumer, scoreCalculator, ssProvider)
    val articles = processor.run()

    articles shouldBe empty
  }

  def extractArticles[T](captor: ArgumentCaptor[Map[Long, T]]): Iterable[T] = {
    val m = captor.getValue
    m.values
  }
}

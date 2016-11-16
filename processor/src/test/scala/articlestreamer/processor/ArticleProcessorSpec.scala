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

import scala.collection.mutable

/**
  * Created by sam on 16/10/2016.
  */
class ArticleProcessorSpec extends BaseSpec with SharedSparkContext with DataFrameSuiteBase  with BeforeAndAfter {

  class TestConfig extends ConfigLoader {
    override val tweetsBatchSize: Int = 1
  }

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

  it should "retrieve articles and update scores, then return in proper order" in {
    val article = TwitterArticle("00000000-0000-0000-0000-000000000001", "789070025009336320", new Date(123456789l),
      mutable.WrappedArray.empty, "", TweetAuthor(1234, "user1", 0), Some(20))
    val article2 = TwitterArticle("00000000-0000-0000-0000-000000000002", "789070025044436320", new Date(123456789l),
      mutable.WrappedArray.empty, "", TweetAuthor(5678, "user2", 0), Some(10))

    when(consumer.pullAll()).thenReturn(List(article, article2))
    val mapCaptor: ArgumentCaptor[Map[Long, TwitterArticle]] = ArgumentCaptor.forClass(classOf[Map[Long, TwitterArticle]])
    when(scoreCalculator.updateScores(mapCaptor.capture()))
      .thenReturn(List(article))
      .thenReturn(List(article2))

    val processor = new ArticleProcessor(config, consumer, scoreCalculator, ssProvider)
    val articles = processor.run()

    articles should have length 2
    articles(0).id shouldBe article2.id
    articles(1).id shouldBe article.id

    val processedArticles = mapCaptor.getAllValues
    processedArticles.get(0) should have size 1
    processedArticles.get(0)(789070025009336320l).id shouldBe "00000000-0000-0000-0000-000000000001"
    processedArticles.get(1) should have size 1
    processedArticles.get(1)(789070025044436320l).id shouldBe "00000000-0000-0000-0000-000000000002"
  }

  it should "fail to retrieve scores" in {
    val article = TwitterArticle("00000000-0000-0000-0000-000000000001", "789070025009336320", new Date(123456789l),
      List(), "", TweetAuthor(1234, "user1", 0), Some(20))
    val article2 = TwitterArticle("00000000-0000-0000-0000-000000000002", "789070025044436320", new Date(123456789l),
      mutable.WrappedArray.empty, "", TweetAuthor(5678, "user2", 0), Some(10))

    when(consumer.pullAll()).thenReturn(List(article, article2))

    when(scoreCalculator.updateScores(any()))
        .thenReturn(List(article))
      .thenThrow(new RuntimeException())

    val processor = new ArticleProcessor(config, consumer, scoreCalculator, ssProvider)
    val articles = processor.run()

    articles should have size 1
    articles.head.id shouldBe article.id

  }

  it should "process an empty queue" in {
    when(consumer.pullAll()).thenReturn(List())

    val processor = new ArticleProcessor(config, consumer, scoreCalculator, ssProvider)
    val articles = processor.run()

    articles shouldBe empty
  }

  def extractArticles[T](captor: ArgumentCaptor[Map[Long, T]]): Iterable[T] = {
    val m = captor.getValue
    m.values
  }
}

package articlestreamer.processor

import articlestreamer.processor.kafka.KafkaConsumerWrapper
import articlestreamer.processor.spark.SparkSessionProvider
import articlestreamer.shared.BaseSpec
import articlestreamer.shared.configuration.ConfigLoader
import articlestreamer.shared.model.TwitterArticle
import articlestreamer.shared.scoring.{NaiveTwitterScoreCalculator, TwitterScoreCalculator}
import articlestreamer.shared.twitter.service.TwitterService
import com.holdenkarau.spark.testing.{DataFrameSuiteBase, SharedSparkContext}
import org.hamcrest.Matchers
import org.hamcrest.Matchers.{any => _, _}
import org.mockito.ArgumentCaptor
import org.mockito.Mockito._
import org.mockito.ArgumentMatchers._
import twitter4j.{Twitter, TwitterFactory}

import scala.io.Source

/**
  * Created by sam on 16/10/2016.
  */
class ArticleProcessorSpec extends BaseSpec with SharedSparkContext with DataFrameSuiteBase {

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
    when(consumer.poll(any(), any())).thenReturn(List(tweet1))

    val mapCaptor: ArgumentCaptor[Map[Long, TwitterArticle]] = ArgumentCaptor.forClass(classOf[Map[Long, TwitterArticle]])
    when(scoreCalculator.updateScores(mapCaptor.capture())).thenReturn(List())

    val processor = new ArticleProcessor(config, consumer, scoreCalculator, ssProvider)
    processor.run()

    val processedArticles = mapCaptor.getValue
    processedArticles should have size 1
    processedArticles(789070025009336320l).id shouldBe "00000000-0000-0000-0000-000000000001"
  }

  def extractArticles[T](captor: ArgumentCaptor[Map[Long, T]]): Iterable[T] = {
    val m = captor.getValue
    m.values
  }
}

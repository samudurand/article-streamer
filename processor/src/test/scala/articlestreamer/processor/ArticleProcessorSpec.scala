package articlestreamer.processor

import articlestreamer.processor.kafka.KafkaConsumerWrapper
import articlestreamer.processor.spark.SparkSessionProvider
import articlestreamer.shared.BaseSpec
import articlestreamer.shared.configuration.ConfigLoader
import articlestreamer.shared.scoring.NaiveTwitterScoreCalculator
import articlestreamer.shared.twitter.service.TwitterService
import com.holdenkarau.spark.testing.{DataFrameSuiteBase, SharedSparkContext}
import org.mockito.Mockito._
import twitter4j.{Twitter, TwitterFactory}

/**
  * Created by sam on 16/10/2016.
  */
class ArticleProcessorSpec extends BaseSpec with SharedSparkContext with DataFrameSuiteBase {

  class TestConfig extends ConfigLoader
  val config = new TestConfig

  val factory: TwitterFactory = mock(classOf[TwitterFactory])

  "" should "run" in {
    when(factory.getInstance).thenReturn(mock(classOf[Twitter]))

    val ssProvider = mock(classOf[SparkSessionProvider])
    when(ssProvider.getSparkSession).thenReturn(spark)

    val consumer = mock(classOf[KafkaConsumerWrapper])
    //when(consumer.poll(any(), any())).thenReturn()

    val twitterService: TwitterService = mock(classOf[TwitterService])
    //when(twitterService.getTweetsDetails())

    val scoreCalculator = mock(classOf[NaiveTwitterScoreCalculator])

    val processor = new ArticleProcessor(config, consumer, twitterService, scoreCalculator, ssProvider)
    //processor.run()
  }

}

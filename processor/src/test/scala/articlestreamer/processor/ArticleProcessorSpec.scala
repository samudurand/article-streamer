package articlestreamer.processor

import articlestreamer.processor.kafka.KafkaConsumerWrapper
import articlestreamer.processor.service.TwitterService
import articlestreamer.processor.spark.SparkSessionProvider
import articlestreamer.shared.BaseSpec
import articlestreamer.shared.configuration.ConfigLoader
import com.holdenkarau.spark.testing.{DataFrameSuiteBase, SharedSparkContext}
import twitter4j.{Twitter, TwitterFactory}
import twitter4j.auth.AccessToken

import org.mockito.Mockito._
import org.mockito.ArgumentMatchers._

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

    val processor = new ArticleProcessor(config, consumer, twitterService, ssProvider)
    //processor.run()
  }

}

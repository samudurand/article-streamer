package articlestreamer.shared.kafka

import articlestreamer.shared.BaseSpec
import articlestreamer.shared.configuration.ConfigLoader
import org.joda.time.DateTimeUtils

/**
  * Created by sam on 19/11/2016.
  */
class HalfDayTopicManagerSpec extends BaseSpec {

  class TestConfig extends ConfigLoader {
    override val kafkaFirstTopic: String = "topic1"
    override val kafkaSecondTopic: String = "topic2"
  }

  val config = new TestConfig
  val topicManager = new HalfDayTopicManager(config)

  "HalfDayTopicManager" should "return first topic during morning" in {
    DateTimeUtils.setCurrentMillisFixed(1182128400000l)
    topicManager.getCurrentTopic() shouldBe "topic1"
    DateTimeUtils.setCurrentMillisSystem()
  }

  it should "return second topic during afternoon" in {
    DateTimeUtils.setCurrentMillisFixed(1182171600000l)
    topicManager.getCurrentTopic() shouldBe "topic2"
    DateTimeUtils.setCurrentMillisSystem()
  }

  it should "return first topic at midnight" in {
    DateTimeUtils.setCurrentMillisFixed(1182124800000l)
    topicManager.getCurrentTopic() shouldBe "topic1"
    DateTimeUtils.setCurrentMillisSystem()
  }

  it should "return second topic at noon" in {
    DateTimeUtils.setCurrentMillisFixed(1182168000000l)
    topicManager.getCurrentTopic() shouldBe "topic2"
    DateTimeUtils.setCurrentMillisSystem()
  }
}

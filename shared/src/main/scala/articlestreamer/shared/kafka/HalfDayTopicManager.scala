package articlestreamer.shared.kafka

import articlestreamer.shared.configuration.ConfigLoader
import org.joda.time.LocalTime

/**
  * Handle the choice of topic. At the moment it simply choose based on the hour of the day (00 to 12 and 12 to 24)
  */
class HalfDayTopicManager(config: ConfigLoader) extends DualTopicManager {

  val MIDNIGHT = new LocalTime(0, 0, 0)
  val NOON = new LocalTime(12, 0, 0)

  def getCurrentTopic(): String = {
    val now = new LocalTime()
    if (now.isAfter(MIDNIGHT) && now.isBefore(NOON)) {
      getFirstTopic()
    } else {
      getSecondTopic()
    }
  }

  override def getTopicList(): Array[String] = Array(config.kafkaFirstTopic, config.kafkaSecondTopic)

  override def getFirstTopic(): String = config.kafkaFirstTopic

  override def getSecondTopic(): String = config.kafkaSecondTopic
}

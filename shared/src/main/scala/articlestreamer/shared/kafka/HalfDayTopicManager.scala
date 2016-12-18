package articlestreamer.shared.kafka

import articlestreamer.shared.configuration.ConfigLoader
import org.joda.time.{DateTimeZone, LocalTime}

/**
  * Handle the choice of topic. At the moment it simply choose based on the hour of the day (00 to 12 and 12 to 24)
  */
class HalfDayTopicManager(config: ConfigLoader) extends DualTopicManager {

  val MIDNIGHT = new LocalTime(0, 0, 0)
  val NOON = new LocalTime(12, 0, 0)

  def getCurrentTopic(): String = {
    val now = new LocalTime(DateTimeZone.UTC)
    if ((now.isEqual(MIDNIGHT) || now.isAfter(MIDNIGHT)) && now.isBefore(NOON)) {
      getFirstTopic()
    } else {
      getSecondTopic()
    }
  }

  def getNotCurrentTopic(): String = {
    val currentTopic = getCurrentTopic()
    if (currentTopic == getFirstTopic()) {
      getSecondTopic()
    } else {
      getFirstTopic()
    }
  }

  override def getTopicList(): Array[String] = Array(config.kafkaFirstTopic, config.kafkaSecondTopic)

  override def getFirstTopic(): String = config.kafkaFirstTopic

  // Put back when out of testing phase
  override def getSecondTopic(): String = config.kafkaSecondTopic
}

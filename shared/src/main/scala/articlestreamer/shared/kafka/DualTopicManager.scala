package articlestreamer.shared.kafka


/**
  * Define a Topic Manager handling two topics.
  */
trait DualTopicManager {

  def getTopicList(): Array[String]

  def getCurrentTopic(): String

  def getNotCurrentTopic(): String

  def getFirstTopic(): String

  def getSecondTopic(): String

}

package articlestreamer.shared.model.kafka

import java.sql.Date

import articlestreamer.shared.model.TwitterArticle

/**
  * A record containing an article or used as end of message queue
  * @param date when the record was created
  * @param endOfQueue true if that message is the end of the message queue
  * @param article contains an article if it not the end of queue
  */
case class KafkaRecord(date: Date, endOfQueue: Boolean, article: Option[TwitterArticle]) {

  /**
    * Build a record containing an article
    * @param date
    * @param article
    */
  def this(date: Date, article: TwitterArticle) {
    this(date, false, Some(article))
  }

  /**
    * Build an end of queue record
    * @param date
    */
  def this(date: Date) {
    this(date, true, None)
  }

}

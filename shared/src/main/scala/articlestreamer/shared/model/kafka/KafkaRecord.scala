package articlestreamer.shared.model.kafka

import java.sql.Date

import articlestreamer.shared.model.TwitterArticle

/**
  * A record containing an article or used as end of message queue
  * @param date when the record was created
  * @param endOfQueue true if that message is the end of the message queue
  * @param article contains an article if it not the end of queue
  */
class KafkaRecord(val date: Date,
                           val endOfQueue: Boolean,
                           val article: Option[TwitterArticle])

case class ArticleRecord(override val date: Date, override val article: Option[TwitterArticle], override val endOfQueue: Boolean = false) extends KafkaRecord(date, endOfQueue, article)

case class EndOfQueueRecord(override val date: Date) extends KafkaRecord(date, true, None)

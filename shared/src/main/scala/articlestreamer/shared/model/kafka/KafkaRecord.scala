package articlestreamer.shared.model.kafka

import java.sql.Date

import articlestreamer.shared.model.{Article, TwitterArticle}

/**
  * A record containing an article or used as end of message queue
  * @param date when the record was created
  * @param endOfQueue true if that message is the end of the message queue
  * @param article contains an article if it not the end of queue
  */
abstract class KafkaRecord(date: Date,
                       endOfQueue: Boolean,
                       article: Option[TwitterArticle])

case class ArticleRecord(date: Date, article: TwitterArticle) extends KafkaRecord(date, false, Some(article))

case class EndOfQueueRecord(date: Date) extends KafkaRecord(date, true, None)

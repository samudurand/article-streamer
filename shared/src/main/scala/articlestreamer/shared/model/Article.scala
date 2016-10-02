package articlestreamer.shared.model

import java.sql.Timestamp
import java.time.LocalDate
import java.util.UUID

import articlestreamer.shared.model.ArticleSource.ArticleSource

sealed trait Article

/**
 * Generic article class
 * @param id supposed to be a UUID
 * @param source source from which originate this article (Twitter, Linkedin...)
 * @param originalId source original Id
 * @param publicationDate date on which that article was published on the source
 * @param score the highest the score, the higher the probability that this article is worth your while
 */
class BaseArticle(id: String,
                  source: ArticleSource,
                  originalId: String,
                  publicationDate: Timestamp,
                  score: Option[Int]) extends Article

/**
 * Tweet supposed to contain a reference to an article
 * @param links list of links contained in the tweet
 * @param content body of the tweet
 */
case class TwitterArticle(id: String,
                          originalId: String,
                          publicationDate: Timestamp,
                          links: Seq[String],
                          content: String,
                          score: Option[Int])
  extends BaseArticle(id, ArticleSource.Twitter, originalId, publicationDate, score)
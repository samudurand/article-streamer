package articlestreamer.shared.model

import java.time.LocalDate
import java.util.UUID

import articlestreamer.shared.model.ArticleSource.ArticleSource

trait Article

class BaseArticle(id: UUID,
                  source: ArticleSource,
                  originalId: String,
                  publicationDate: LocalDate,
                  link: List[String],
                  description: String,
                  score: Option[Int]) extends Article

case class TwitterArticle(id: UUID,
                          originalId: String,
                          publicationDate: LocalDate,
                          link: List[String],
                          description: String,
                          score: Option[Int])
  extends BaseArticle(id, ArticleSource.Twitter, originalId, publicationDate, link, description, score)

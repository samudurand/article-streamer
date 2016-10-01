package articlestreamer.shared.model

import java.time.LocalDate
import java.util.UUID

import articlestreamer.shared.model.ArticleSource.ArticleSource

trait Article

class BaseArticle(id: UUID,
                     source: ArticleSource,
                     originalId: String,
                     createdAt: LocalDate,
                     link: List[String],
                     description: String) extends Article

case class TwitterArticle(id: UUID,
                          originalId: String,
                          createdAt: LocalDate,
                          link: List[String],
                          description: String)
  extends BaseArticle(id, ArticleSource.Twitter, originalId, createdAt, link, description)
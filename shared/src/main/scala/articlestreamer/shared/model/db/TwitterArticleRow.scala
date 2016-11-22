package articlestreamer.shared.model.db

import java.sql.Date

import articlestreamer.shared.model.TwitterArticle

/**
  * Created by sam on 22/11/2016.
  */
case class TwitterArticleRow(id: String,
                             originalId: String,
                             publicationDate: Date,
                             content: String,
                             author: Long,
                             score: Int) {

  def this(article: TwitterArticle) = {
    this(article.id,
      article.originalId,
      article.publicationDate,
      article.content,
      article.author.id,
      article.score.getOrElse(0))
  }

}

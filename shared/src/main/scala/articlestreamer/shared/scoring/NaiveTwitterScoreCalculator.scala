package articlestreamer.shared.scoring

import articlestreamer.shared.configuration.ConfigLoader
import articlestreamer.shared.model.{TweetPopularity, TwitterArticle}
import articlestreamer.shared.twitter.service.TwitterService

class NaiveTwitterScoreCalculator(configLoader: ConfigLoader, twitterService: TwitterService) extends TwitterScoreCalculator {

  import configLoader._

  private val config = twitterConfig.searchConfig

  private val VALUE_RELATED_TAGS = 100
  private val VALUE_ARTICLE_CLOSE_WORDS = 500
  private val VALUE_ARTICLE_RELATED_WORDS = 100
  private val VALUE_SUBJECT_CLOSE_WORDS = 100
  private val VALUE_SUBJECT_RELATED_WORDS = 20
  private val COST_ARTICLE_UNRELATED_WORDS = -200
  private val COST_SUBJECT_UNRELATED_WORDS = -50

  protected val wordsAndValues = List(
    (config.relatedTags, VALUE_RELATED_TAGS),
    (config.articleCloseWords, VALUE_ARTICLE_CLOSE_WORDS),
    (config.articleRelatedWords, VALUE_ARTICLE_RELATED_WORDS),
    (config.subjectCloseWords, VALUE_SUBJECT_CLOSE_WORDS),
    (config.subjectRelatedWords, VALUE_SUBJECT_RELATED_WORDS),
    (config.articleUnrelatedWords, COST_ARTICLE_UNRELATED_WORDS),
    (config.relatedTags, COST_SUBJECT_UNRELATED_WORDS)
  )

  override def calculateBaseScore(article: TwitterArticle): Int = {
    val score = 0

    wordsAndValues.foldLeft(score) { case (acc, (words, value)) =>
      acc + value * countOccurrences(words, article.content)
    }
  }

  /**
   * Count the occurrences of the words from the list in the text
   */
  private def countOccurrences(wordsToSearch: List[String], toAnalyse: String): Int = {
    wordsToSearch.foldLeft(0)((acc, word) => acc + s"""\\b$word\\b""".r.findAllIn(toAnalyse).length)
  }

  override def updateScores(articlesById: Map[Long, TwitterArticle]): Traversable[TwitterArticle] = {
    twitterService.getTweetsPopularities(articlesById.keys.toList).map {
      case (id, Some(details: TweetPopularity)) =>
        val article = articlesById(id)
        val updatedScore = calculateTweetScore(article, details)
        article.copy(score = Some(updatedScore))
      case (id, None) => articlesById(id)
    }
  }

  private def calculateTweetScore(article: TwitterArticle, popularity: TweetPopularity): Int = {
    article.score.getOrElse(0) + popularity.retweetCount + popularity.favoriteCount * 2
  }
}

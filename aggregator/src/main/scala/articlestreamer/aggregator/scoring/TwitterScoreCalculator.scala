package articlestreamer.aggregator.scoring

import articlestreamer.shared.configuration.ConfigLoader
import articlestreamer.shared.model.TwitterArticle

trait TwitterScoreCalculator extends ScoreCalculator[TwitterArticle] {

  val config = ConfigLoader.twitterSearchConfig

  val VALUE_RELATED_TAGS = 100

  val VALUE_ARTICLE_CLOSE_WORDS = 500
  val VALUE_ARTICLE_RELATED_WORDS = 100

  val VALUE_SUBJECT_CLOSE_WORDS = 100
  val VALUE_SUBJECT_RELATED_WORDS = 20

  val COST_ARTICLE_UNRELATED_WORDS = -200
  val COST_SUBJECT_UNRELATED_WORDS = -50

  val wordsAndValues = List(
    (config.relatedTags, VALUE_RELATED_TAGS),
    (config.articleCloseWords, VALUE_ARTICLE_CLOSE_WORDS),
    (config.articleRelatedWords, VALUE_ARTICLE_RELATED_WORDS),
    (config.subjectCloseWords, VALUE_SUBJECT_CLOSE_WORDS),
    (config.subjectRelatedWords, VALUE_SUBJECT_RELATED_WORDS),
    (config.articleUnrelatedWords, COST_ARTICLE_UNRELATED_WORDS),
    (config.relatedTags, COST_SUBJECT_UNRELATED_WORDS)
  )

  override def calculateBaseScore(article: TwitterArticle): Int = {
    val score = 1

    wordsAndValues.foldLeft(score) { case (acc, (words, value)) =>
      acc + value * countOccurrences(words, article.content)
    }
  }

  /**
   * Count the occurrences of the words from the list in the text
   */
  private def countOccurrences(wordsToSearch: List[String], toAnalyse: String): Int = {
    wordsToSearch.foldLeft(0)((acc, word) => acc + s"""\b$word\b""".r.findAllIn(toAnalyse).length)
  }

}

package articlestreamer.shared.scoring

import articlestreamer.shared.model.TwitterArticle

trait TwitterScoreCalculator extends ScoreCalculator[TwitterArticle] {

  def updateScores(articlesById: Map[Long, TwitterArticle]): Traversable[TwitterArticle]

}

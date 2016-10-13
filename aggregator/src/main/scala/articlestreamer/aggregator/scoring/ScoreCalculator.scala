package articlestreamer.aggregator.scoring

import articlestreamer.shared.model.Article

trait ScoreCalculator[T <: Article] {

  def calculateBaseScore(article: T): Int

}

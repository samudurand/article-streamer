package articlestreamer.aggregator.scoring

import org.scalamock.scalatest.MockFactory
import org.scalatest.{FlatSpec, Matchers}
import articlestreamer.aggregator.scoring.NaiveTwitterScoreCalculator
import articlestreamer.shared.configuration.{ConfigLoader, TwitterSearchConfig}
import articlestreamer.shared.model.TwitterArticle
import com.typesafe.config.ConfigFactory

class NaiveTwitterScoreCalculatorSpec extends FlatSpec with MockFactory with Matchers with ConfigLoader with NaiveTwitterScoreCalculator {

  override val wordsAndValues = List(
    (List("close1", "close2"), 1000),
    (List("related3", "related4"), 100),
    (List("unrelated1", "unrelated2"), -200)
  )

  "A tweet with no words of any import" should "get a 0 base score" in {
    val content = "other1 other2 other3"
    val article = TwitterArticle("", "", null, null, content, null)
    val score = calculateBaseScore(article)

    score shouldBe 0
  }

  "A tweet with close words" should "get a good base score" in {
    val content = "related4 close1 other close2 related3"
    val article = TwitterArticle("", "", null, null, content, null)
    val score = calculateBaseScore(article)

    score shouldBe 2200
  }

  "A tweet with somewhat interesting words" should "get an average base score" in {
    val content = "related4 other some related3 other"
    val article = TwitterArticle("", "", null, null, content, null)
    val score = calculateBaseScore(article)

    score shouldBe 200
  }

  "A tweet with unrelated words" should "get a bad score" in {
    val content = "related4 other some related3 other"
    val article = TwitterArticle("", "", null, null, content, null)
    val score = calculateBaseScore(article)

    score shouldBe 200
  }

}

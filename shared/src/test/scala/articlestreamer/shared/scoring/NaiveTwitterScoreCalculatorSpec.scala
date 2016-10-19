package articlestreamer.shared.scoring

import java.sql.Timestamp

import articlestreamer.shared.BaseSpec
import articlestreamer.shared.configuration.ConfigLoader
import articlestreamer.shared.model.{TweetPopularity, TwitterArticle}
import articlestreamer.shared.twitter.service.TwitterService
import org.hamcrest.CoreMatchers
import org.mockito.ArgumentMatchers
import org.mockito.Mockito._
import org.mockito.ArgumentMatchers._
import org.scalatest.BeforeAndAfter


class NaiveTwitterScoreCalculatorSpec extends BaseSpec with BeforeAndAfter {

  class TestConfigLoader extends ConfigLoader

  class TestCalculator(config: ConfigLoader, twitterService: TwitterService) extends NaiveTwitterScoreCalculator(config, twitterService) {
    override val wordsAndValues = List(
      (List("close1", "close2"), 1000),
      (List("related3", "related4"), 100),
      (List("unrelated1", "unrelated2"), -200)
    )
  }

  var twitterService: TwitterService = _
  var scoreCalculator: TwitterScoreCalculator = _


  before {
    twitterService = mock(classOf[TwitterService])
    scoreCalculator = new TestCalculator(new TestConfigLoader(), twitterService)
  }

  "A tweet with no words of any import" should "get a 0 base score" in {
    val content = "other1 other2 other3"
    val article = TwitterArticle("", "", null, null, content, null)
    val score = scoreCalculator.calculateBaseScore(article)

    score shouldBe 0
  }

  "A tweet with no content" should "get a 0 base score" in {
    val content = ""
    val article = TwitterArticle("", "", null, null, content, null)
    val score = scoreCalculator.calculateBaseScore(article)

    score shouldBe 0
  }

  "A tweet with close words" should "get a good base score" in {
    val content = "related4 close1 other close2 related3"
    val article = TwitterArticle("", "", null, null, content, null)
    val score = scoreCalculator.calculateBaseScore(article)

    score shouldBe 2200
  }

  "A tweet with somewhat interesting words" should "get an average base score" in {
    val content = "related4 other some related3 other"
    val article = TwitterArticle("", "", null, null, content, null)
    val score = scoreCalculator.calculateBaseScore(article)

    score shouldBe 200
  }

  "A tweet with unrelated words" should "get a bad score" in {
    val content = "unrelated1 other some unrelated2 related3"
    val article = TwitterArticle("", "", null, null, content, null)
    val score = scoreCalculator.calculateBaseScore(article)

    score should be < 0
  }

  "Tweets not popular" should "keep a base score" in {
    val articles = Map(1l -> TwitterArticle("id1", "1", null, List(), "", Some(0)),
                       2l -> TwitterArticle("id2", "2", null, List(), "", Some(0)))
    val notPopular = TweetPopularity(0, 0)
    when(twitterService.getTweetsDetails(any()))
      .thenReturn(Map(1l -> Some(notPopular), 2l -> Some(notPopular)))

    val updateArticles = scoreCalculator.updateScores(articles)

    updateArticles shouldBe articles.values.toList
  }

  "Tweets with no initial scores" should "should get at least a 0 score" in {
    val articles = Map(1l -> TwitterArticle("id1", "1", null, List(), "", None),
                       2l -> TwitterArticle("id2", "2", null, List(), "", None))
    val notPopular = TweetPopularity(0, 0)
    val popular = TweetPopularity(100, 100)
    when(twitterService.getTweetsDetails(any()))
      .thenReturn(Map(1l -> Some(notPopular), 2l -> Some(popular)))

    val updatedArticles = scoreCalculator.updateScores(articles).toList

    updatedArticles(0).score.get shouldBe 0
    updatedArticles(1).score.get shouldBe 300
  }

  "Tweets favorited" should "get an improved score" in {
    val articles = Map(1l -> TwitterArticle("id1", "1", null, List(), "", Some(0)),
                       2l -> TwitterArticle("id2", "2", null, List(), "", Some(0)),
                       3l -> TwitterArticle("id3", "3", null, List(), "", Some(100)))

    val notPopular = TweetPopularity(0, 0)
    val quitePopular = TweetPopularity(0, 10)
    val veryPopular = TweetPopularity(0, 1000)

    when(twitterService.getTweetsDetails(any()))
      .thenReturn(Map(1l -> Some(quitePopular), 2l -> Some(notPopular), 3l -> Some(veryPopular)))

    val updatedArticles = scoreCalculator.updateScores(articles).toList

    updatedArticles(0).score.get shouldBe 20
    updatedArticles(1).score.get shouldBe 0
    updatedArticles(2).score.get shouldBe 2100
  }

  "Tweets retweeted" should "get an improved score" in {
    val articles = Map(1l -> TwitterArticle("id1", "1", null, List(), "", Some(0)),
                       2l -> TwitterArticle("id2", "2", null, List(), "", Some(0)),
                       3l -> TwitterArticle("id3", "3", null, List(), "", Some(100)))

    val notPopular = TweetPopularity(0, 0)
    val quitePopular = TweetPopularity(20, 0)
    val veryPopular = TweetPopularity(500, 0)

    when(twitterService.getTweetsDetails(any()))
      .thenReturn(Map(1l -> Some(quitePopular), 2l -> Some(notPopular), 3l -> Some(veryPopular)))

    val updatedArticles = scoreCalculator.updateScores(articles).toList

    updatedArticles(0).score.get shouldBe 20
    updatedArticles(1).score.get shouldBe 0
    updatedArticles(2).score.get shouldBe 600
  }

  "Tweets retweeted and favorited" should "get an even more improved score" in {
    val articles = Map(1l -> TwitterArticle("id1", "1", null, List(), "", Some(0)),
                       2l -> TwitterArticle("id2", "2", null, List(), "", Some(0)),
                       3l -> TwitterArticle("id3", "3", null, List(), "", Some(100)))

    val popular1 = TweetPopularity(1, 1000)
    val popular2 = TweetPopularity(20, 10)
    val popular3 = TweetPopularity(100, 1)

    when(twitterService.getTweetsDetails(any()))
      .thenReturn(Map(1l -> Some(popular2), 2l -> Some(popular1), 3l -> Some(popular3)))

    val updatedArticles = scoreCalculator.updateScores(articles).toList

    updatedArticles(0).score.get shouldBe 40
    updatedArticles(1).score.get shouldBe 2001
    updatedArticles(2).score.get shouldBe 202
  }

  "Tweets for which the popularity is not retrievable" should "keep the base score" in {
    val articles = Map(1l -> TwitterArticle("id1", "1", null, List(), "", Some(10)),
                       2l -> TwitterArticle("id2", "2", null, List(), "", Some(50)),
                       3l -> TwitterArticle("id3", "3", null, List(), "", Some(100)))

    val popular = TweetPopularity(100, 1)

    when(twitterService.getTweetsDetails(any()))
      .thenReturn(Map(2l -> Some(popular), 1l -> None, 3l -> None))

    val updatedArticles = scoreCalculator.updateScores(articles).toList

    updatedArticles(0).score.get shouldBe 152
    updatedArticles(1).score.get shouldBe 10
    updatedArticles(2).score.get shouldBe 100
  }

}

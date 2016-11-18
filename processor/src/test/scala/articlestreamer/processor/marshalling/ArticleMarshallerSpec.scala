package articlestreamer.processor.marshalling

import java.text.SimpleDateFormat
import java.util.TimeZone

import articlestreamer.shared.BaseSpec
import articlestreamer.shared.model.TweetAuthor

import scala.io.Source

class ArticleMarshallerSpec extends BaseSpec with ArticleMarshaller {

  val df: SimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss")
  df.setTimeZone(TimeZone.getTimeZone("GMT"))

  "Marshaller" should "unmarshall a tweet" in {
    val articleJson = Source.fromURL(getClass.getResource("/data/record-twitter-article.json")).mkString

    val articleTweet = unmarshallArticle(articleJson)

    articleTweet.isDefined shouldBe true
    articleTweet.get should have(
      'id ("00000000-0000-0000-0000-000000000001"),
      'originalId ("789070025009336320"),
      'publicationDate (df.parse("2016-10-20 11:45:28.000")),
      'links (List("https://t.co/C5m0dEKan9")),
      'content ("Well done! Tough challenge to master #Spark https://t.co/C5m0dEKan9"),
      'author (TweetAuthor(1234, "user1", 0)),
      'score (Some(0))
    )
  }

  it should "unmarshall a tweet without score" in {
    val articleJson = Source.fromURL(getClass.getResource("/data/record-twitter-article-no-score.json")).mkString

    val articleTweet = unmarshallArticle(articleJson)

    articleTweet.isDefined shouldBe true
    articleTweet.get should have('score (None))
  }

  it should "fail to unmarshall a tweet with missing data" in {
    val articleJson = Source.fromURL(getClass.getResource("/data/record-twitter-article-bad-formatting.json")).mkString

    val articleTweet = unmarshallArticle(articleJson)

    articleTweet shouldBe None
  }

}
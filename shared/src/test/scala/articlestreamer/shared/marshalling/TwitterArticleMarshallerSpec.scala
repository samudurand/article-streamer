package articlestreamer.shared.marshalling

import java.text.SimpleDateFormat
import java.util.TimeZone

import articlestreamer.shared.BaseSpec
import articlestreamer.shared.model.TweetAuthor

import scala.io.Source

class TwitterArticleMarshallerSpec extends BaseSpec with TwitterArticleMarshaller {

  val df: SimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss")
  df.setTimeZone(TimeZone.getTimeZone("GMT"))

  "Marshaller" should "unmarshall a tweet" in {
    val articleJson = Source.fromURL(getClass.getResource("/data/record-twitter-article.json")).mkString

    val articleTweet = unmarshallArticle(articleJson)

    articleTweet.isDefined shouldBe true
    articleTweet.get should have(
      'id ("d0fa3a2a-74a9-49d5-8fac-b12f810f29b8"),
      'originalId ("808316754900484096"),
      'links (List("https://twitter.com/")),
      'content ("#spark test https://t.co/WLUWq7roAm"),
      'author (TweetAuthor(1935423961, "Firenssam", 3)),
      'score (Some(10))
    )
    articleTweet.get.publicationDate.getTime shouldBe 1481552706000l
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

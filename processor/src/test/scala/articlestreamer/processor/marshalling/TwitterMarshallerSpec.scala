package articlestreamer.processor.marshalling

import java.sql.Timestamp

import articlestreamer.shared.BaseSpec

import scala.io.Source

class TwitterMarshallerSpec extends BaseSpec {

  import TwitterMarshaller._

  "Marshaller" should "unmarshall a tweet" in {
    val articleJson = Source.fromURL(getClass.getResource("/data/twitter-article.json")).mkString

    val articleTweet = unmarshallTwitterArticle(articleJson)

    articleTweet.isDefined shouldBe true
    articleTweet.get should have(
      'id ("00000000-0000-0000-0000-000000000001"),
      'originalId ("789070025009336320"),
      'publicationDate (Timestamp.valueOf("2016-10-20 11:45:28.000")),
      'links (List("https://t.co/C5m0dEKan9")),
      'content ("Well done! Tough challenge to master #Spark https://t.co/C5m0dEKan9"),
      'score (Some(0))
    )
  }

}

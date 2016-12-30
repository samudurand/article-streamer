package articlestreamer.aggregator.twitter.utils

import articlestreamer.shared.BaseSpec
import org.mockito.Mockito._
import twitter4j.{Status, URLEntity}

class TwitterStatusMethodSpec extends BaseSpec with TwitterStatusMethods {

  "Status with English content" should "be accepted" in {
    val status = mock(classOf[Status])
    when(status.getLang).thenReturn("en")

    status.containsEnglish shouldBe true
  }

  "Status with other language than English content" should "be rejected" in {
    val status = mock(classOf[Status])
    when(status.getLang).thenReturn("fr")

    status.containsEnglish shouldBe false
  }

  "Status with a page url" should "return a single URL" in {
    val urlPage = buildEntity("http://thenextweb.com/e-dash-display")
    val urlImg = buildEntity("https://pbs.twimg.com/1234.jpeg")
    val facebookUrl = buildEntity("https://www.facebook.com/62648/photos/352127.62868648/1222842638/?type=3&theater")
    val instagramUrl = buildEntity("https://www.instagram.com/?hl=en")
    val urls = Array(facebookUrl, instagramUrl, urlPage, urlImg)

    val status = mock(classOf[Status])
    when(status.getURLEntities).thenReturn(urls)

    status.getUsableLinks(List()) shouldBe Array(urlPage)
  }

  "Status with only a page url" should "return a single URL" in {
    val urlPage = buildEntity("http://thenextweb.com/e-dash-display")
    val urls = Array(urlPage)

    val status = mock(classOf[Status])
    when(status.getURLEntities).thenReturn(urls)

    status.getUsableLinks(List()) shouldBe Array(urlPage)
  }

  "Status with only media urls" should "return an empty list" in {
    val urlImg = buildEntity("https://pbs.twimg.com/1234.jpeg")
    val urls = Array(urlImg)

    val status = mock(classOf[Status])
    when(status.getURLEntities).thenReturn(urls)

    status.getUsableLinks(List()) shouldBe empty
  }

  "Status with only social networks urls" should "return empty list" in {
    val facebookUrl = buildEntity("https://www.facebook.com/62648/photos/352127.62868648/1222842638/?type=3&theater")
    val instagramUrl = buildEntity("https://www.instagram.com/?hl=en")
    val urls = Array(instagramUrl, facebookUrl)

    val status = mock(classOf[Status])
    when(status.getURLEntities).thenReturn(urls)

    status.getUsableLinks(List()) shouldBe empty
  }

  "Status with only urls from ignored domains" should "return empty list" in {
    val adsUrl = buildEntity("https://adsDomain.com/62648/photos/352127.62868648/1222842638/?type=3&theater")

    val status = mock(classOf[Status])
    when(status.getURLEntities).thenReturn(Array(adsUrl))

    status.getUsableLinks(List("otherUrl", "adsDomain.com")) shouldBe empty
  }

  "Status with no urls" should "return empty list" in {
    val status = mock(classOf[Status])
    when(status.getURLEntities).thenReturn(Array[URLEntity]())
    status.getUsableLinks(List()) shouldBe empty
  }

  private def buildEntity(url: String): URLEntity = {
    val entity = mock(classOf[URLEntity])
    when(entity.getExpandedURL).thenReturn(url)
    entity
  }

}

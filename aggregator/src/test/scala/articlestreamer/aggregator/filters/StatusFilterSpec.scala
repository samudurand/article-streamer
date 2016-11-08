package articlestreamer.aggregator.filters

import articlestreamer.shared.BaseSpec
import org.mockito.Mockito._
import twitter4j.{Status, URLEntity}

class StatusFilterSpec extends BaseSpec with TwitterStatusMethods {

  "Status with at least one page url" should "be identified as a potential article" in {
    val urlPage = buildEntity("http://thenextweb.com/e-dash-display")
    val urlImg = buildEntity("https://pbs.twimg.com/1234.jpeg")
    val urls = Array(urlPage, urlImg)

    val status = mock(classOf[Status])
    when(status.getURLEntities).thenReturn(urls)

    status.isPotentialArticle shouldBe true
  }

  "Status with only a page url" should "be identified as a potential article" in {
    val urlPage = buildEntity("http://thenextweb.com/e-dash-display")
    val urls = Array(urlPage)

    val status = mock(classOf[Status])
    when(status.getURLEntities).thenReturn(urls)

    status.isPotentialArticle shouldBe true
  }

  "Status with only media urls" should "be rejected" in {
    val urlImg = buildEntity("https://pbs.twimg.com/1234.jpeg")
    val urlVideo = buildEntity("https://pbs.twimg.com/34567.mov")
    val urls = Array(urlVideo, urlImg)

    val status = mock(classOf[Status])
    when(status.getURLEntities).thenReturn(urls)

    status.isPotentialArticle shouldBe false
  }

  private def buildEntity(url: String): URLEntity = {
    val entity = mock(classOf[URLEntity])
    when(entity.getExpandedURL).thenReturn(url)
    entity
  }

}

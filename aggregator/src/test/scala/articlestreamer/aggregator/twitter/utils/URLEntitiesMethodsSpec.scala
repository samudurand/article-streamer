package articlestreamer.aggregator.twitter.utils

import articlestreamer.shared.BaseSpec
import org.mockito.Mockito._
import twitter4j.URLEntity

/**
  * Created by sam on 08/11/2016.
  */
class URLEntitiesMethodsSpec extends BaseSpec with URLEntitiesMethods {

  "Domain from HTTP URLEntity" should "be retrievable" in {
    val httpUrl = buildEntity("http://thenextweb.com/e-dash-display?source=twitter")
    httpUrl.domain shouldBe "thenextweb.com"
  }

  "Domain from HTTPS URLEntity" should "be retrievable" in {
    val httpsUrl = buildEntity("https://thenextweb.com/e-dash-display?source=twitter")
    httpsUrl.domain shouldBe "thenextweb.com"
  }

  "Domain from other protocol URLEntity" should "not be retrievable" in {
    val ftpUrl = buildEntity("ftp://thenextweb.com/e-dash-display?source=twitter")
    ftpUrl.domain shouldBe ""
  }

  "Domain from weirdly formatted URLEntity" should "not be retrievable" in {
    val weirdUrl = buildEntity("http://thenextweb.com\\e-dash-display?source=twitter")
    weirdUrl.domain shouldBe ""
  }

  "URL Entity" should "not contain an image url" in {
    val urlPage = buildEntity("http://thenextweb.com/e-dash-display")
    urlPage.isImageUrl shouldBe false
  }

  it should "contain an image url" in {
    val urlImg = buildEntity("https://pbs.twimg.com/1234.jpeg")
    urlImg.isImageUrl shouldBe true
  }

  it should "not contain a video url" in {
    val urlPage = buildEntity("http://thenextweb.com/e-dash-display")
    urlPage.isVideoUrl shouldBe false
  }

  it should "contain a video url" in {
    val urlVideo = buildEntity("https://pbs.twimg.com/1234.mov")
    urlVideo.isVideoUrl shouldBe true
  }

  it should "not contain a media url" in {
    val urlPage = buildEntity("http://thenextweb.com/e-dash-display")
    urlPage.isUnusableMediaUrl shouldBe false
  }

  it should "contain a media url" in {
    val urlImg = buildEntity("https://pbs.twimg.com/1234.jpg")
    urlImg.isUnusableMediaUrl shouldBe true
  }

  it should "contain a social network url" in {
    val urlPage = buildEntity("http://www.facebook.com/62648/photos/352127.62868648/1222842638/?type=3&theater")
    urlPage.isSocialNetworkURL shouldBe true
  }

  it should "contain a social network secured url" in {
    val urlPage = buildEntity("https://www.facebook.com/62648/photos/352127.62868648/1222842638/?type=3&theater")
    urlPage.isSocialNetworkURL shouldBe true
  }

  it should "not contain a social network url" in {
    val urlPage = buildEntity("http://thenextweb.com/e-dash-display")
    urlPage.isSocialNetworkURL shouldBe false
  }

  private def buildEntity(url: String): URLEntity = {
    val entity = mock(classOf[URLEntity])
    when(entity.getExpandedURL).thenReturn(url)
    entity
  }

}

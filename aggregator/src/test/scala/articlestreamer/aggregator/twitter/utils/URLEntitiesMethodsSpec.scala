package articlestreamer.aggregator.twitter.utils

import articlestreamer.shared.BaseSpec
import org.mockito.Mockito._
import twitter4j.URLEntity

/**
  * Created by sam on 08/11/2016.
  */
class URLEntitiesMethodsSpec extends BaseSpec with URLEntitiesMethods {

  "URL Entity" should "not contain an image url" in {
    val urlPage = buildEntity("http://thenextweb.com/e-dash-display")
    urlPage.isImageUrl shouldBe false
  }

  "URL Entity" should "contain an image url" in {
    val urlImg = buildEntity("https://pbs.twimg.com/1234.jpeg")
    urlImg.isImageUrl shouldBe true
  }

  "URL Entity" should "not contain a video url" in {
    val urlPage = buildEntity("http://thenextweb.com/e-dash-display")
    urlPage.isVideoUrl shouldBe false
  }

  "URL Entity" should "contain a video url" in {
    val urlVideo = buildEntity("https://pbs.twimg.com/1234.mov")
    urlVideo.isVideoUrl shouldBe true
  }

  "URL Entity" should "not contain a media url" in {
    val urlPage = buildEntity("http://thenextweb.com/e-dash-display")
    urlPage.isMediaUrl shouldBe false
  }

  "URL Entity" should "contain a media url" in {
    val urlVideo = buildEntity("https://pbs.twimg.com/1234.mp4")
    val urlImg = buildEntity("https://pbs.twimg.com/1234.jpg")

    urlVideo.isMediaUrl shouldBe true
    urlImg.isMediaUrl shouldBe true
  }

  "URL Entity" should "contain a social network url" in {
    val urlPage = buildEntity("http://www.facebook.com/62648/photos/352127.62868648/1222842638/?type=3&theater")
    urlPage.isSocialNetworkURL shouldBe true
  }

  "URL Entity" should "contain a social network secured url" in {
    val urlPage = buildEntity("https://www.facebook.com/62648/photos/352127.62868648/1222842638/?type=3&theater")
    urlPage.isSocialNetworkURL shouldBe true
  }

  "URL Entity" should "not contain a social network url" in {
    val urlPage = buildEntity("http://thenextweb.com/e-dash-display")
    urlPage.isSocialNetworkURL shouldBe false
  }

  private def buildEntity(url: String): URLEntity = {
    val entity = mock(classOf[URLEntity])
    when(entity.getExpandedURL).thenReturn(url)
    entity
  }

}

package articlestreamer.aggregator.filters

import articlestreamer.shared.BaseSpec
import org.mockito.Mockito._
import twitter4j.URLEntity

/**
  * Created by sam on 08/11/2016.
  */
class URLEntitiesUtilsSpec extends BaseSpec with URLEntitiesMethods {

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

  private def buildEntity(url: String): URLEntity = {
    val entity = mock(classOf[URLEntity])
    when(entity.getExpandedURL).thenReturn(url)
    entity
  }

}

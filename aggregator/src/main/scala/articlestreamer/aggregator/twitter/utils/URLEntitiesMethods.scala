package articlestreamer.aggregator.twitter.utils

import twitter4j.URLEntity

trait URLEntitiesMethods {

  val imgTypes = Array(".bmp", ".gif", ".img", ".jbg", ".jpe", ".jpeg", ".jpg", ".png", ".ppm", ".tiff")
  val videoTypes = Array(".avi", ".flv", ".mpg", ".mp2", ".mpeg", ".mpe", ".mpv", ".mov", ".mp4")
  val socialNetworks = Array("www.instagram", "www.facebook", "www.flickr", "twitter")

  implicit class URLEntitiesUtils(urlEntity: URLEntity) {

    def isImageUrl: Boolean = {
      imgTypes.exists(urlEntity.getExpandedURL.endsWith(_))
    }

    def isVideoUrl: Boolean = {
      videoTypes.exists(urlEntity.getExpandedURL.endsWith(_))
    }

    def isMediaUrl: Boolean = {
      urlEntity.isImageUrl || urlEntity.isVideoUrl
    }

    def isSocialNetworkURL: Boolean = {
      socialNetworks.exists(social =>
        urlEntity.getExpandedURL.startsWith(s"https://$social")
          || urlEntity.getExpandedURL.startsWith(s"http://$social"))
    }

  }

}

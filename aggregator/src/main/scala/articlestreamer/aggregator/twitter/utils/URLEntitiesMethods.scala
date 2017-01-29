package articlestreamer.aggregator.twitter.utils

import com.typesafe.scalalogging.Logger
import twitter4j.URLEntity

trait URLEntitiesMethods {

  val imgTypes = Array(".bmp", ".gif", ".img", ".jbg", ".jpe", ".jpeg", ".jpg", ".png", ".ppm", ".tiff")
  val videoTypes = Array(".avi", ".flv", ".mpg", ".mp2", ".mpeg", ".mpe", ".mpv", ".mov", ".mp4")
  val socialNetworks = Array("www.instagram", "www.facebook", "www.flickr", "twitter")

  implicit class URLEntitiesUtils(urlEntity: URLEntity) {

    def domain: String = {
      val logger = Logger(classOf[URLEntity])

      val shortUrl = urlEntity.getExpandedURL match {
        case url if url.startsWith("https://") => Some(url.drop(8))
        case url if url.startsWith("http://") => Some(url.drop(7))
        case _ =>
          logger.warn(s"Encountered an unknown URL protocol : ${urlEntity.getExpandedURL}")
          None
      }

      shortUrl match {
        case Some(url) if url.contains("/") =>
          url.substring(0, url.indexOf("/"))
        case _ =>
          logger.warn(s"Encountered an unknown URL format : ${urlEntity.getExpandedURL}")
          ""
      }
    }

    def isImageUrl: Boolean = {
      imgTypes.exists(urlEntity.getExpandedURL.endsWith(_))
    }

    def isVideoUrl: Boolean = {
      videoTypes.exists(urlEntity.getExpandedURL.endsWith(_))
    }

    def isUnusableMediaUrl: Boolean = {
      // Currently accepting videos, might reject them in the future
      urlEntity.isImageUrl
    }

    def isSocialNetworkURL: Boolean = {
      socialNetworks.exists(social =>
        urlEntity.getExpandedURL.startsWith(s"https://$social")
          || urlEntity.getExpandedURL.startsWith(s"http://$social"))
    }

  }

}

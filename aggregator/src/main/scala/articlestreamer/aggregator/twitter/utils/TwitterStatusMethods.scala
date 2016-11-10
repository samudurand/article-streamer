package articlestreamer.aggregator.twitter.utils

import twitter4j.Status

/**
  * Created by sam on 08/11/2016.
  */
trait TwitterStatusMethods extends URLEntitiesMethods {

  implicit class StatusFilter(status: Status) {

    def isPotentialArticle: Boolean = {

      val links = status.getURLEntities

      // An article must contain at least one link
      if (links.length <= 0) {
        return false
      }

      // At least one of those links isn't a media
      !links.forall(link => link.isMediaUrl || link.isSocialNetworkURL)
    }

  }

}

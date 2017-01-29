package articlestreamer.aggregator.twitter.utils

import twitter4j.{Status, URLEntity}

/**
  * Created by sam on 08/11/2016.
  */
trait TwitterStatusMethods extends URLEntitiesMethods {

  implicit class StatusFilter(status: Status) {

    def containsEnglish: Boolean = {
      status.getLang == "en"
    }

    /**
      * Identify all links in this Status that could be links to articles.
      * This excludes any social network link as well as media urls.
      */
    def getUsableLinks(ignoredDomains: List[String]): Array[URLEntity] = {

      val links = status.getURLEntities

      // An article must contain at least one link
      if (links.length <= 0) {
        return Array()
      }

      // At least one of those links isn't a media
      links.filterNot(link => link.isUnusableMediaUrl || link.isSocialNetworkURL || ignoredDomains.contains(link.domain))
    }

  }

}

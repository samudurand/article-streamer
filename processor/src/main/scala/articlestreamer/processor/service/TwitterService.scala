package articlestreamer.processor.service

import articlestreamer.processor.model.TweetPopularity
import articlestreamer.shared.configuration.ConfigLoader
import articlestreamer.shared.exception.exceptions._
import articlestreamer.shared.twitter.TwitterAuthorizationConfig
import twitter4j.auth.AccessToken
import twitter4j.{ResponseList, Status, Twitter, TwitterFactory}

import scala.collection.JavaConversions._

class TwitterService(config: ConfigLoader) {

  val authorizationConfig = TwitterAuthorizationConfig.getTwitterConfig(config)
  val twitter: Twitter  = new TwitterFactory().getInstance()
  twitter.setOAuthConsumer(authorizationConfig.getOAuthConsumerKey, authorizationConfig.getOAuthConsumerSecret)

  val accessToken: AccessToken = new AccessToken(authorizationConfig.getOAuthAccessToken, authorizationConfig.getOAuthAccessTokenSecret)
  twitter.setOAuthAccessToken(accessToken)

  def getTweetsDetails(ids: List[Long]): Map[Long, Option[TweetPopularity]] = {
    try {

      val responseList: ResponseList[Status] = twitter.lookup(ids:_*)

      val popularities = responseList
        .map(status => (status.getId, Some(TweetPopularity(status.getRetweetCount, status.getFavoriteCount))))
        .toMap

      val originalNum = ids.size
      val retrievedNum = responseList.size()
      if (retrievedNum != originalNum) {
        println(s"WARN : Only $retrievedNum on $originalNum tweets could be retrieved.")
        addMissingPopularities(ids, popularities)
      } else {
        popularities
      }

    } catch {
      case ex: Exception =>
        ex.printNeatStackTrace()
        Map()
    }
  }

  def addMissingPopularities(ids: List[Long], popularities: Map[Long, Option[TweetPopularity]]): Map[Long, Option[TweetPopularity]] = {
    val retrievedIds = popularities.keySet
    val missingPopularities =
      ids
        .filterNot(id => retrievedIds.contains(id))
        .foldLeft(Map[Long, Option[TweetPopularity]]()) ( (acc, id) => acc + (id -> None))
    popularities ++ missingPopularities
  }

  def getTweetDetails(tweetId: Long): Option[TweetPopularity] = {
    try {
      val status: Status = twitter.showStatus(tweetId)
      Some(TweetPopularity(status.getRetweetCount, status.getFavoriteCount))
    } catch {
      case ex: Exception =>
        ex.printNeatStackTrace()
        None
    }
  }


}

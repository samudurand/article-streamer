package articlestreamer.shared.twitter.service

import java.util.function.Consumer

import articlestreamer.shared.configuration.ConfigLoader
import articlestreamer.shared.exception.exceptions._
import articlestreamer.shared.model.TweetPopularity
import articlestreamer.shared.twitter.TwitterAuthorizationConfig
import twitter4j.auth.AccessToken
import twitter4j.{ResponseList, Status, Twitter, TwitterFactory}

import scala.collection.mutable

class TwitterService(config: ConfigLoader, twitterFactory: TwitterFactory) {

  private val authorizationConfig = TwitterAuthorizationConfig.getTwitterConfig(config)

  private val twitter: Twitter  = twitterFactory.getInstance()
  twitter.setOAuthConsumer(authorizationConfig.getOAuthConsumerKey, authorizationConfig.getOAuthConsumerSecret)

  private val accessToken: AccessToken = new AccessToken(authorizationConfig.getOAuthAccessToken, authorizationConfig.getOAuthAccessTokenSecret)
  twitter.setOAuthAccessToken(accessToken)

  def getTweetsPopularities(ids: List[Long]): Map[Long, Option[TweetPopularity]] = {
    try {

      val responseList: ResponseList[Status] = twitter.lookup(ids:_*)

      // Use of foreach instead of scala added functions to allow mocking
      var popularities: mutable.Map[Long, Option[TweetPopularity]] = mutable.Map()
      responseList.forEach(new Consumer[Status]() {
          override def accept(status: Status): Unit = {
            popularities += status.getId -> Some(TweetPopularity(status.getRetweetCount, status.getFavoriteCount))
          }
        })

      val originalNum = ids.size
      val retrievedNum = responseList.size()
      if (retrievedNum != originalNum) {
        println(s"WARN : Only $retrievedNum on $originalNum tweets could be retrieved.")
        addMissingPopularities(ids, popularities.toMap)
      } else {
        popularities.toMap
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

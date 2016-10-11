package articlestreamer.processor.service

import java.util.stream.Collectors

import articlestreamer.processor.model.TweetPopularity
import articlestreamer.shared.exception.exceptions._
import articlestreamer.shared.twitter.TwitterAuthorizationConfig
import twitter4j.auth.AccessToken
import twitter4j.{ResponseList, Status, TwitterFactory, Twitter}
import scala.collection.JavaConversions._

trait TwitterService extends TwitterAuthorizationConfig {

  val twitter: Twitter  = new TwitterFactory().getInstance()
  twitter.setOAuthConsumer(twitterConfig.getOAuthConsumerKey, twitterConfig.getOAuthConsumerSecret)

  val accessToken: AccessToken = new AccessToken(twitterConfig.getOAuthAccessToken, twitterConfig.getOAuthAccessTokenSecret)
  twitter.setOAuthAccessToken(accessToken)

  def getTweetsDetails(ids: List[Long]): Map[Long, TweetPopularity] = {
    try {

      val responseList: ResponseList[Status] = twitter.lookup(ids:_*)

      val originalNum = ids.size
      val retrievedNum = responseList.size()
      if (retrievedNum != originalNum) {
        println(s"WARN : Only $retrievedNum on $originalNum tweets could be retrieved.")
      }

      responseList
        .map(status => (status.getId, TweetPopularity(status.getRetweetCount, status.getFavoriteCount)))
        .toMap

    } catch {
      case ex: Exception =>
        ex.printNeatStackTrace()
        Map()
    }
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

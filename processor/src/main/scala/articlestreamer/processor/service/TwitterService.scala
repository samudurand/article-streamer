package articlestreamer.processor.service

import javax.ws.rs.core.HttpHeaders

import articlestreamer.processor.model.TweetDetails
import articlestreamer.shared.twitter.TwitterAuthorizationConfig
import twitter4j.auth.AccessToken
import twitter4j.{TwitterFactory, Twitter}

trait TwitterService extends TwitterAuthorizationConfig {

  val twitter: Twitter  = new TwitterFactory().getInstance()
  twitter.setOAuthConsumer(twitterConfig.getOAuthConsumerKey, twitterConfig.getOAuthConsumerSecret)

  val accessToken: AccessToken = new AccessToken(twitterConfig.getOAuthAccessToken, twitterConfig.getOAuthAccessTokenSecret)
  twitter.setOAuthAccessToken(accessToken)

  def getTweet(tweetId: Long): Option[TweetDetails] = {

    try {
      val status = twitter.showStatus(tweetId)
      println(status)
    } catch {
      case ex: Exception => System.err.println(ex)
    }

    Some(TweetDetails())
  }


}

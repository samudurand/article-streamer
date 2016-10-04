package articlestreamer.shared.configuration

import com.typesafe.config.ConfigFactory

object ConfigLoader {

  private val appConfig = ConfigFactory.load()

  val twitterOuathConsumerKey = appConfig.getString("twitter.oauth.oauthConsumerKey")
  val twitterOauthConsumerSecret = appConfig.getString("twitter.oauth.oauthConsumerSecret")
  val twitterOauthAccessToken = appConfig.getString("twitter.oauth.oauthAccessToken")
  val twitterOauthAccessTokenSecret = appConfig.getString("twitter.oauth.oauthAccessTokenSecret")

}

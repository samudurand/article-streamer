package articlestreamer.shared.twitter

import com.typesafe.config.ConfigFactory

trait TwitterAuthorizationConfig {

  private val appConfig = ConfigFactory.load()

  val twitterConfig = new twitter4j.conf.ConfigurationBuilder()
    .setJSONStoreEnabled(true)
    .setOAuthConsumerKey(appConfig.getString("twitter.oauth.oauthConsumerKey"))
    .setOAuthConsumerSecret(appConfig.getString("twitter.oauth.oauthConsumerSecret"))
    .setOAuthAccessToken(appConfig.getString("twitter.oauth.oauthAccessToken"))
    .setOAuthAccessTokenSecret(appConfig.getString("twitter.oauth.oauthAccessTokenSecret"))
    .build
}

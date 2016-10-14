package articlestreamer.shared.twitter

import articlestreamer.shared.configuration.ConfigLoader
import com.typesafe.config.ConfigFactory

trait TwitterAuthorizationConfig {

  self: ConfigLoader =>

  val twitterConfig = new twitter4j.conf.ConfigurationBuilder()
    .setJSONStoreEnabled(true)
    .setOAuthConsumerKey(twitterOuathConsumerKey)
    .setOAuthConsumerSecret(twitterOauthConsumerSecret)
    .setOAuthAccessToken(twitterOauthAccessToken)
    .setOAuthAccessTokenSecret(twitterOauthAccessTokenSecret)
    .build
}

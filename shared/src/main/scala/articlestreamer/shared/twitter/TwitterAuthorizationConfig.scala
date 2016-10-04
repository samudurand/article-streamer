package articlestreamer.shared.twitter

import articlestreamer.shared.configuration.ConfigLoader
import com.typesafe.config.ConfigFactory

trait TwitterAuthorizationConfig {

  val twitterConfig = new twitter4j.conf.ConfigurationBuilder()
    .setJSONStoreEnabled(true)
    .setOAuthConsumerKey(ConfigLoader.twitterOuathConsumerKey)
    .setOAuthConsumerSecret(ConfigLoader.twitterOauthConsumerSecret)
    .setOAuthAccessToken(ConfigLoader.twitterOauthAccessToken)
    .setOAuthAccessTokenSecret(ConfigLoader.twitterOauthAccessTokenSecret)
    .build
}

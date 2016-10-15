package articlestreamer.shared.twitter

import articlestreamer.shared.configuration.ConfigLoader
import com.typesafe.config.ConfigFactory

object TwitterAuthorizationConfig {

  def getTwitterConfig(config: ConfigLoader) = {
    new twitter4j.conf.ConfigurationBuilder()
      .setJSONStoreEnabled(true)
      .setOAuthConsumerKey(config.twitterOuathConsumerKey)
      .setOAuthConsumerSecret(config.twitterOauthConsumerSecret)
      .setOAuthAccessToken(config.twitterOauthAccessToken)
      .setOAuthAccessTokenSecret(config.twitterOauthAccessTokenSecret)
      .build
  }
}

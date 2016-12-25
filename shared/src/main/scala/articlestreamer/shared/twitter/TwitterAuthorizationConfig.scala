package articlestreamer.shared.twitter

import articlestreamer.shared.configuration.ConfigLoader
import twitter4j.conf.Configuration

object TwitterAuthorizationConfig {

  def getTwitterConfig(config: ConfigLoader): Configuration = {
    val authConfig = config.twitterConfig.authConfig
    new twitter4j.conf.ConfigurationBuilder()
      .setJSONStoreEnabled(true)
      .setOAuthConsumerKey(authConfig.consumerKey)
      .setOAuthConsumerSecret(authConfig.consumerSecret)
      .setOAuthAccessToken(authConfig.accessToken)
      .setOAuthAccessTokenSecret(authConfig.accessSecret)
      .build
  }
}

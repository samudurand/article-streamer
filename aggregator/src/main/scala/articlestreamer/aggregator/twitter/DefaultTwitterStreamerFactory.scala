package articlestreamer.aggregator.twitter

import articlestreamer.shared.configuration.ConfigLoader
import twitter4j.Status

class DefaultTwitterStreamerFactory extends TwitterStreamerFactory {

  override def getStreamer(config: ConfigLoader, tweetHandler: (Status) => Unit, stopHandler: () => Unit): TwitterStreamer = {
    DefaultTwitterStreamer(config, tweetHandler, stopHandler)
  }

}

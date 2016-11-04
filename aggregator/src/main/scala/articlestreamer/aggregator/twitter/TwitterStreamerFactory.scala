package articlestreamer.aggregator.twitter
import articlestreamer.shared.configuration.ConfigLoader
import twitter4j.Status

/**
  * Created by sam on 04/11/2016.
  */
trait TwitterStreamerFactory {

  def getStreamer(config: ConfigLoader, tweetHandler: (Status) => Unit, stopHandler: () => Unit): TwitterStreamer

}

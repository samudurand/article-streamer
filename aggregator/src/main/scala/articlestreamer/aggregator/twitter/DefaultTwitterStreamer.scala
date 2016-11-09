package articlestreamer.aggregator.twitter

import articlestreamer.shared.configuration.ConfigLoader
import articlestreamer.shared.twitter.TwitterAuthorizationConfig
import com.typesafe.scalalogging.LazyLogging
import twitter4j._

case class DefaultTwitterStreamer(config: ConfigLoader,
                                  onStatusFct: (Status) => Unit,
                                  onStop: () => Unit) extends TwitterStreamer with LazyLogging {

  val stream = new TwitterStreamFactory(TwitterAuthorizationConfig.getTwitterConfig(config)).getInstance

  def statusListener = new StatusListener() {
    def onStatus(status: Status) = onStatusFct(status)
    def onDeletionNotice(statusDeletionNotice: StatusDeletionNotice) = {}
    def onTrackLimitationNotice(numberOfLimitedStatuses: Int) = {logger.error(s"Limitation hit : $numberOfLimitedStatuses")}
    def onException(ex: Exception) = { logger.error("Error while streaming tweets", ex) }
    def onScrubGeo(arg0: Long, arg1: Long) = {}
    def onStallWarning(warning: StallWarning) = {}
  }

  override def startStreaming() = {
    logger.info("Twitter Streamer : Starting streaming")
    stream.addListener(statusListener)
    stream.filter(new FilterQuery().track(config.twitterSearchConfig.mainTag))
    logger.info("Twitter Streamer : Streaming started")
  }

  override def stop() = {
    stream.cleanUp()
    stream.shutdown()
    onStop()
  }

}
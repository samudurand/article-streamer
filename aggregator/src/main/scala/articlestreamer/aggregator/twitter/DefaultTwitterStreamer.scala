package articlestreamer.aggregator.twitter

import articlestreamer.shared.configuration.ConfigLoader
import articlestreamer.shared.twitter.TwitterAuthorizationConfig
import com.typesafe.scalalogging.LazyLogging
import twitter4j._

case class DefaultTwitterStreamer(config: ConfigLoader,
                                  onStatusFct: (Status) => Unit,
                                  onStop: () => Unit) extends TwitterStreamer with LazyLogging {

  private val stream = new TwitterStreamFactory(TwitterAuthorizationConfig.getTwitterConfig(config)).getInstance

  def statusListener = new StatusListener() {
    override def onStatus(status: Status): Unit = onStatusFct(status)
    override def onDeletionNotice(statusDeletionNotice: StatusDeletionNotice): Unit = {}
    override def onTrackLimitationNotice(numberOfLimitedStatuses: Int): Unit = {logger.error(s"Limitation hit : $numberOfLimitedStatuses")}
    override def onException(ex: Exception): Unit = { logger.error("Error while streaming tweets", ex) }
    override def onScrubGeo(arg0: Long, arg1: Long): Unit = {}
    override def onStallWarning(warning: StallWarning): Unit = {}
  }

  override def startStreaming(): Unit = {
    logger.info("Twitter Streamer : Starting streaming")
    stream.addListener(statusListener)
    stream.filter(new FilterQuery().track(config.twitterConfig.searchConfig.mainTag))
    logger.info("Twitter Streamer : Streaming started")
  }

  override def stop(): Unit = {
    stream.cleanUp()
    stream.shutdown()
    onStop()
  }

}
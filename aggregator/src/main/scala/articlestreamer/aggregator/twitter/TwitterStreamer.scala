package articlestreamer.aggregator.twitter

trait TwitterStreamer {

  def startStreaming(): Unit

  def stop(): Unit

}

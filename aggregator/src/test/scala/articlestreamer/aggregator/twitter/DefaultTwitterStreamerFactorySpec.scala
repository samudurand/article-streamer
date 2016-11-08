package articlestreamer.aggregator.twitter

import articlestreamer.shared.BaseSpec
import articlestreamer.shared.configuration.ConfigLoader

class DefaultTwitterStreamerFactorySpec extends BaseSpec {

  class TestConfig extends ConfigLoader

  val factory = new DefaultTwitterStreamerFactory

  "Factory" should "provide default streamer" in {
    val streamer = factory.getStreamer(new TestConfig, null, null)

    streamer shouldBe a [DefaultTwitterStreamer]
  }

}

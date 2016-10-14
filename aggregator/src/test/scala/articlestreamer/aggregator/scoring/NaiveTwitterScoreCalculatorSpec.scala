package articlestreamer.aggregator.scoring

import org.scalamock.scalatest.proxy.MockFactory
import org.scalatest.FlatSpec
import articlestreamer.aggregator.scoring.NaiveTwitterScoreCalculator
import articlestreamer.shared.configuration.ConfigLoader
import com.typesafe.config.ConfigFactory

trait TestConfigLoader extends ConfigLoader {


}

class NaiveTwitterScoreCalculatorSpec extends FlatSpec with MockFactory with TestConfigLoader {

  "" should "have size 0" in {
    assert(Set.empty.size == 0)
  }

}

package articlestreamer.aggregator

import articlestreamer.aggregator.kafka.KafkaProducerWrapper
import articlestreamer.aggregator.scoring.NaiveTwitterScoreCalculator
import articlestreamer.shared.configuration.ConfigLoader
import com.softwaremill.macwire._

/**
  * Created by sam on 15/10/2016.
  */
object MainApp extends App {

  override def main(args: Array[String]) {

    lazy val config = wire[ConfigLoader]
    lazy val scoreCalculator = wire[NaiveTwitterScoreCalculator]
    lazy val producer = wire[KafkaProducerWrapper]

    val aggregator = wire[Aggregator]
    aggregator.run()

  }

}

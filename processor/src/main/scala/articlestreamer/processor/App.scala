package articlestreamer.processor

import articlestreamer.processor.jdbc.ConnectionProviderImpl
import articlestreamer.processor.spark.OnDemandSparkProvider
import articlestreamer.shared.configuration.DefaultConfigLoader
import com.softwaremill.macwire._

object App extends App {

  lazy val configLoader = wire[DefaultConfigLoader]
  lazy val sparkProvider = wire[OnDemandSparkProvider]
  lazy val connectionProvider = wire[ConnectionProviderImpl]

  val processor = wire[Processor]
  processor()

}

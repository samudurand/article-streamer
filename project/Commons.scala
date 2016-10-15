import sbt._
import Keys._

object Commons {
  val appVersion = "1.0.0"
  val scalaV = "2.11.8"

  // Used as main class for the whole project
  val producerMainClass = Some("articlestreamer.aggregator.MainApp")

  val settings: Seq[Def.Setting[_]] = Seq(
    version := appVersion,
    scalaVersion := scalaV
  )
}
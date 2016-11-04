import sbt._
import Keys._
import scoverage.ScoverageKeys._

object Commons {
  val appVersion = "1.0.0"
  val scalaV = "2.11.8"

  // Used as main class for the whole project
  val producerMainClass = Some("articlestreamer.aggregator.MainApp")

  val settings: Seq[Def.Setting[_]] = Seq(
    version := appVersion,
    scalaVersion := scalaV,

    coverageEnabled := true,
    coverageMinimum := 95,
    coverageFailOnMinimum := true,

    cleanKeepFiles ++= Seq("resolution-cache", "streams").map(target.value / _)
  )
}
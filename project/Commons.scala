import sbt.Keys._
import sbt._
import scoverage.ScoverageKeys._

object Commons {
  val appVersion = "0.0.1"
  val scalaV = "2.11.8"

  // Used as main class for the whole project
  val producerMainClass = Some("articlestreamer.aggregator.MainApp")

  val settings: Seq[Def.Setting[_]] = Seq(
    version := appVersion,
    scalaVersion := scalaV,

    coverageEnabled := true,
    coverageMinimum := 95,
    coverageFailOnMinimum := true,

    // Flags for style checking
    scalacOptions ++= Seq(
      "-feature",
      "-deprecation",
      "-unchecked",
      "-Xlint",
      "-Xfuture",
      "-Ywarn-unused",
      "-Ywarn-unused-import",
      "-Ywarn-numeric-widen"
    ),

    addCompilerPlugin("com.github.ghik" %% "silencer-plugin" % "0.5"),

    cleanKeepFiles ++= Seq("resolution-cache", "streams").map(target.value / _),

    dependencyOverrides ++= Set(
      "org.scala-lang.modules" %% "scala-xml" % "1.0.5",
      "org.json4s" %% "json4s-jackson" % "3.4.1",
      "commons-io" % "commons-io" % "2.4"
    )

  )
}
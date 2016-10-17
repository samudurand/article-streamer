import sbt._

object Dependencies {

  val kafkaClientVersion = "0.10.0.1"
  val twitter4JVersion = "[4.0,)"

  val commonDependencies: Seq[ModuleID] = Seq(
    "joda-time" % "joda-time" % "2.9.4",
    "com.typesafe" % "config" % "1.3.0",
    "org.json4s" %% "json4s-native" % "3.4.1",
    "org.json4s" %% "json4s-jackson" % "3.4.1",
    "com.softwaremill.macwire" %% "macros" % "2.2.4" % "provided",
    "com.softwaremill.macwire" %% "util" % "2.2.4",
    "com.softwaremill.macwire" %% "proxy" % "2.2.4",

    "org.scalatest" % "scalatest_2.11" % "3.0.0" % "test",
    "org.mockito" % "mockito-core" % "2.2.1" % "test",
    "com.holdenkarau" %% "spark-testing-base" % "2.0.0_0.4.7" % "test"

  )

}
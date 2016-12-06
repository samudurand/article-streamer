import sbt._

object Dependencies {

  val kafkaClientVersion = "0.10.1.0"
  val twitter4JVersion = "[4.0,)"

  val commonDependencies: Seq[ModuleID] = Seq(
    "joda-time" % "joda-time" % "2.9.4",
    "com.typesafe" % "config" % "1.3.0",
    "org.json4s" %% "json4s-native" % "3.4.1",
    "org.json4s" %% "json4s-jackson" % "3.4.1",
    "com.softwaremill.macwire" %% "macros" % "2.2.4" % "provided",
    "com.softwaremill.macwire" %% "util" % "2.2.4",
    "com.softwaremill.macwire" %% "proxy" % "2.2.4",
    "ch.qos.logback" % "logback-classic" % "1.1.7" exclude("org.slf4j","slf4j-api"),
    "com.typesafe.scala-logging" %% "scala-logging" % "3.5.0",
    "com.github.ghik" %% "silencer-lib" % "0.5",

    "org.scalatest" % "scalatest_2.11" % "3.0.0" % "test",
    "org.mockito" % "mockito-core" % "2.2.1" % "test",
    "org.hamcrest" % "hamcrest-all" % "1.3" % "test"
  )

}
import sbt._

object Dependencies {

  val kafkaClientVersion = "0.10.0.1"
  val twitter4JVersion = "[4.0,)"

  val commonDependencies: Seq[ModuleID] = Seq(
    "joda-time" % "joda-time" % "2.9.4",
    "com.typesafe" % "config" % "1.3.0",
    "org.json4s" %% "json4s-native" % "3.4.1",
    "org.json4s" %% "json4s-jackson" % "3.4.1"
  )

}
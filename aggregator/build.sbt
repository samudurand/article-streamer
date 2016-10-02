name := "aggregator"

version := "1.0"

scalaVersion := "2.11.8"

libraryDependencies += "org.apache.kafka" % "kafka-clients" % "0.10.0.1"

libraryDependencies += "org.twitter4j" % "twitter4j-stream" % "[4.0,)"

libraryDependencies += "joda-time" % "joda-time" % "2.9.4"

libraryDependencies += "com.typesafe" % "config" % "1.3.0"

libraryDependencies += "org.json4s" %% "json4s-native" % "3.4.1"

libraryDependencies += "org.json4s" %% "json4s-jackson" % "3.4.1"
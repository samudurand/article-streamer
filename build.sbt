import com.heroku.sbt.HerokuPlugin.autoImport._
import sbt.Keys._
import scoverage.ScoverageKeys._

javaOptions ++= Seq("-Xms512M", "-Xmx2048M", "-XX:MaxPermSize=2048M", "-XX:+CMSClassUnloadingEnabled")

// Prevent reloading dependencies after each `clean`
cleanKeepFiles ++= Seq("resolution-cache", "streams").map(target.value / _)

// Build with tests and coverage
addCommandAlias("build-agg", ";project aggregator;clean;coverage;test;coverageReport")
addCommandAlias("build-proc", ";project processor;clean;coverage;test;coverageReport")

lazy val root = (project in file(".")).
  settings(Commons.settings: _*).
  settings(
    name := "article-streamer",

    // sbt-assembly for FatJar generation
    mainClass in assembly <<= (mainClass in assembly in aggregator),

    // sbt run by default starts the Aggregator
    run in Compile <<= (run in Compile in aggregator),
    packageBin in Compile <<= (packageBin in Compile in aggregator),

    // Heroku configuration
    herokuAppName in Compile := "article-streamer-aggregator",
    herokuFatJar in Compile := Some((assemblyOutputPath in assembly).value),
    herokuProcessTypes in Compile := Map(
      "worker" -> "java -jar target/scala-2.11/article-streamer-assembly-1.0.0.jar")

  ) dependsOn (aggregator % "test->test;compile->compile") aggregate(aggregator)

lazy val aggregator = (project in file("aggregator")).
  settings(Commons.settings: _*).
  settings(
    name := "aggregator",

    mainClass in (Compile, run) := Commons.producerMainClass,
    mainClass in (Compile, packageBin) := Commons.producerMainClass,
    mainClass in assembly := Commons.producerMainClass,

    libraryDependencies ++= Dependencies.commonDependencies,
    libraryDependencies += "org.apache.kafka" % "kafka-clients"     % Dependencies.kafkaClientVersion,
    libraryDependencies += "org.twitter4j"    % "twitter4j-stream"  % Dependencies.twitter4JVersion,

    coverageExcludedPackages := ".*BasicConsumer;.*MainApp"

  ) dependsOn (shared % "test->test;compile->compile")

lazy val processor = (project in file("processor")).
  settings(Commons.settings: _*).
  settings(
    name := "processor",

    libraryDependencies ++= Dependencies.commonDependencies,
    libraryDependencies += "org.apache.kafka" % "kafka-clients"     % Dependencies.kafkaClientVersion,
    libraryDependencies += "org.apache.spark" %% "spark-core"       % "2.0.0",
    libraryDependencies += "org.apache.spark" %% "spark-sql"        % "2.0.0",
    libraryDependencies += "org.apache.spark" %% "spark-streaming"  % "2.0.0",
    libraryDependencies += "org.apache.spark" %% "spark-hive"       % "2.0.0" % "test",
//    libraryDependencies += "org.apache.spark" %% "spark-streaming-kafka-0-10" % "2.0.0",
    libraryDependencies += "org.scalaj"       %% "scalaj-http"      % "2.3.0",
    libraryDependencies += "org.twitter4j"    % "twitter4j-stream"  % Dependencies.twitter4JVersion

  ) dependsOn (shared % "test->test;compile->compile")

lazy val shared = (project in file("shared")).
  settings(Commons.settings: _*).
  settings(
    name := "shared",

    libraryDependencies += "org.apache.kafka" % "kafka-clients"     % Dependencies.kafkaClientVersion,
    libraryDependencies ++= Dependencies.commonDependencies,
    libraryDependencies += "org.twitter4j" % "twitter4j-stream" % Dependencies.twitter4JVersion

  )

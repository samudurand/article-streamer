import com.heroku.sbt.HerokuPlugin.autoImport._
import sbt.Keys._
import scoverage.ScoverageKeys._

javaOptions ++= Seq("-Xms1024M", "-Xmx2048M", "-XX:MaxPermSize=2048M", "-XX:+CMSClassUnloadingEnabled")

// Necessary for using
parallelExecution in Test := false
libraryDependencies += "mysql" % "mysql-connector-java" % "5.1.40"

// Prevent reloading dependencies after each `clean`
//cleanKeepFiles ++= Seq("resolution-cache", "streams").map(target.value / _)

// Test with coverage
addCommandAlias("test-agg", ";project aggregator;clean;coverage;test;coverageReport")
addCommandAlias("test-proc", ";project processor;clean;coverage;test;coverageReport")
addCommandAlias("test-shared", ";project shared;clean;coverage;test;coverageReport")
addCommandAlias("test-all", ";test-agg;test-proc;test-shared")

//noinspection ScalaUnnecessaryParentheses
lazy val root = (project in file(".")).
  settings(Commons.settings: _*).
  settings(
    name := "article-streamer",

    // Necessary for using
    parallelExecution in Test := false,

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

    assemblyOutputPath in assembly := file(s"./aggregator/docker/aggregator-assembly-${version.value}.jar"),
    assemblyMergeStrategy in assembly := {
        case PathList("development.conf") => MergeStrategy.discard
        case x =>
            val oldStrategy = (assemblyMergeStrategy in assembly).value
            oldStrategy(x)
    },

    libraryDependencies ++= Dependencies.commonDependencies,
    libraryDependencies += "org.apache.kafka" % "kafka-clients"     % Dependencies.kafkaClientVersion,
    libraryDependencies += "org.twitter4j"    % "twitter4j-stream"  % Dependencies.twitter4JVersion,
    libraryDependencies += "org.quartz-scheduler" % "quartz" % "2.2.1",

    coverageExcludedPackages := ".*BasicConsumer;.*MainApp;.*DefaultTwitterStreamer"

  ) dependsOn (shared % "test->test;compile->compile")

lazy val processor = (project in file("processor")).
  settings(Commons.settings: _*).
  settings(
    name := "processor",

    // Necessary for using
    parallelExecution in Test := false,

    libraryDependencies ++= Dependencies.commonDependencies,
    libraryDependencies += "org.apache.kafka" % "kafka-clients"     % Dependencies.kafkaClientVersion,
    libraryDependencies += "org.apache.spark" %% "spark-core"       % "2.0.0" exclude("org.slf4j","slf4j-log4j12"),
    libraryDependencies += "org.apache.spark" %% "spark-sql"        % "2.0.0",
    libraryDependencies += "org.apache.spark" %% "spark-streaming"  % "2.0.0",
    libraryDependencies += "mysql" % "mysql-connector-java" % "5.1.40",
    libraryDependencies += "org.twitter4j"    % "twitter4j-stream"  % Dependencies.twitter4JVersion,
    //libraryDependencies += "org.apache.spark" %% "spark-streaming-kafka-0-10" % "2.0.0",
    //libraryDependencies += "org.apache.spark" %% "spark-streaming-kafka-0-8" % "2.0.0",
    //libraryDependencies += "org.scalaj"       %% "scalaj-http"      % "2.3.0",

    libraryDependencies += "com.holdenkarau" %% "spark-testing-base" % "2.0.0_0.4.7" % "test",
    libraryDependencies += "org.apache.spark" %% "spark-hive"       % "2.0.0" % "test",

    coverageExcludedPackages := ".*OnDemandSparkSessionProvider;.*MainApp"

) dependsOn (shared % "test->test;compile->compile")

lazy val shared = (project in file("shared")).
  settings(Commons.settings: _*).
  settings(
    name := "shared",

    // Necessary for using
    parallelExecution in Test := false,

    libraryDependencies += "org.apache.kafka" % "kafka-clients"     % Dependencies.kafkaClientVersion,
    libraryDependencies ++= Dependencies.commonDependencies,
    libraryDependencies += "org.twitter4j" % "twitter4j-stream" % Dependencies.twitter4JVersion,

    coverageExcludedPackages := "articlestreamer\\.shared\\.model\\..*;.*ConfigLoader"

)

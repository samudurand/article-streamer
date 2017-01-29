import com.heroku.sbt.HerokuPlugin.autoImport._
import sbt.Keys._
import scoverage.ScoverageKeys._

javaOptions ++= Seq("-Xms1024M", "-Xmx2048M", "-XX:MaxPermSize=2048M", "-XX:+CMSClassUnloadingEnabled")

// Necessary for using
parallelExecution in Test := false
libraryDependencies += "mysql" % "mysql-connector-java" % "5.1.40"

// Prevent reloading dependencies after each `clean`
//cleanKeepFiles ++= Seq("resolution-cache", "streams").map(target.value / _)

// Build assemblies for docker deployment from root
addCommandAlias("build-agg", ";project aggregator;coverageOff;assembly")
addCommandAlias("build-proc", ";project processor;coverageOff;assembly")
addCommandAlias("build-score", ";project twitterScoreUpdater;coverageOff;assembly")
addCommandAlias("build-all", ";build-agg;build-proc;build-score")

// Test with coverage from root
addCommandAlias("test-agg", ";project aggregator;clean;coverage;test;coverageReport")
addCommandAlias("test-score", ";project twitterScoreUpdater;clean;coverage;test;coverageReport")
addCommandAlias("test-proc", ";project processor;clean;coverage;test;coverageReport")
addCommandAlias("test-shared", ";project shared;clean;coverage;test;coverageReport")
addCommandAlias("test-all", ";test-agg;test-score;test-proc;test-shared")

lazy val aggregator = (project in file("aggregator")).
  settings(Commons.settings: _*).
  settings(
    name := "aggregator",

    mainClass in assembly := Some("articlestreamer.aggregator.App"),

    assemblyOutputPath in assembly := file(s"./aggregator/docker/aggregator-assembly-${version.value}.jar"),
    assemblyMergeStrategy in assembly := {
        case PathList("development.conf") => MergeStrategy.discard
        case PathList("docker-env.list") => MergeStrategy.discard
        case "logback.xml" => MergeStrategy.last
        case x =>
            val oldStrategy = (assemblyMergeStrategy in assembly).value
            oldStrategy(x)
    },

    libraryDependencies ++= Dependencies.commonDependencies,
    libraryDependencies += "net.debasishg"        %% "redisclient"     % "3.2",
    libraryDependencies += "org.apache.kafka"     % "kafka-clients"    % Dependencies.kafkaClientVersion,
    libraryDependencies += "org.quartz-scheduler" % "quartz"           % "2.2.1",
    libraryDependencies += "org.scalaj"           %% "scalaj-http"     % "2.3.0",
    libraryDependencies += "org.twitter4j"        % "twitter4j-stream" % Dependencies.twitter4JVersion,

    coverageExcludedPackages := ".*RedisClientFactory;.*BasicConsumer;.*App;.*DefaultTwitterStreamer"

  ) dependsOn (shared % "test->test;compile->compile")

lazy val processor = (project in file("processor")).
  settings(Commons.settings: _*).
  settings(
    name := "processor",

    // Check that one regularly, at the moment very low to avoid unit testing the main Stream
    coverageMinimum := 55,

    // Necessary for using spark testing base
    parallelExecution in Test := false,

    assemblyOutputPath in assembly := file(s"./processor/docker/processor-assembly-${version.value}.jar"),
    assemblyMergeStrategy in assembly := {
      case PathList("org","aopalliance", xs @ _*) => MergeStrategy.last
      case PathList("javax", "inject", xs @ _*) => MergeStrategy.last
      case PathList("javax", "servlet", xs @ _*) => MergeStrategy.last
      case PathList("javax", "activation", xs @ _*) => MergeStrategy.last
      case PathList("org", "apache", xs @ _*) => MergeStrategy.last
      case "development.conf" => MergeStrategy.discard
      case "docker-env.list" => MergeStrategy.discard
      case x =>
        val oldStrategy = (assemblyMergeStrategy in assembly).value
        oldStrategy(x)
    },

    libraryDependencies ++= Dependencies.commonDependencies,
    libraryDependencies += "ch.epfl.scala"    %% "spores" % "0.4.3",
    libraryDependencies += "mysql"            %  "mysql-connector-java"         % "5.1.40",
    libraryDependencies += "org.apache.spark" %% "spark-core"                   % "2.0.0" exclude("org.slf4j","slf4j-log4j12"),
    libraryDependencies += "org.apache.spark" %% "spark-sql"                    % "2.0.0",
    libraryDependencies += "org.apache.spark" %% "spark-streaming"              % "2.0.0",
    libraryDependencies += "org.apache.spark" %% "spark-streaming-kafka-0-10"   % "2.0.0" exclude("org.slf4j","slf4j-log4j12"),
    libraryDependencies += "com.holdenkarau"  %% "spark-testing-base"           % "2.0.0_0.4.7" % "test",
    libraryDependencies += "org.apache.spark" %% "spark-hive"                   % "2.0.0"       % "test",

    coverageExcludedPackages := ".*OnDemandSparkProvider;.*.App"

) dependsOn (shared % "test->test;compile->compile")

lazy val twitterScoreUpdater = (project in file("twitter-score-updater")).
  settings(Commons.settings: _*).
  settings(
    name := "twitterScoreUpdater",

    assemblyOutputPath in assembly := file(s"./twitter-score-updater/docker/score-updater-assembly-${version.value}.jar"),
    assemblyMergeStrategy in assembly := {
      case PathList("docker-env.list") => MergeStrategy.discard
      case PathList("development.conf") => MergeStrategy.discard
      case x =>
        val oldStrategy = (assemblyMergeStrategy in assembly).value
        oldStrategy(x)
    },

    libraryDependencies ++= Dependencies.commonDependencies,
    libraryDependencies += "org.apache.kafka" % "kafka-clients"     % Dependencies.kafkaClientVersion,
    libraryDependencies += "org.twitter4j"    % "twitter4j-stream"  % Dependencies.twitter4JVersion,

    coverageExcludedPackages := ".*OnDemandSparkSessionProvider;.*.App"

) dependsOn (shared % "test->test;compile->compile")

lazy val shared = (project in file("shared")).
  settings(Commons.settings: _*).
  settings(
    name := "shared",

    libraryDependencies ++= Dependencies.commonDependencies,
    libraryDependencies += "org.apache.kafka" % "kafka-clients" % Dependencies.kafkaClientVersion,
    libraryDependencies += "org.twitter4j" % "twitter4j-stream" % Dependencies.twitter4JVersion,

    coverageExcludedPackages := "articlestreamer\\.shared\\.model\\..*;.*ConfigLoader"
)

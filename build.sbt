import com.heroku.sbt.HerokuPlugin.autoImport._

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

  ) dependsOn (aggregator) aggregate(aggregator)

lazy val aggregator = (project in file("aggregator")).
  settings(Commons.settings: _*).
  settings(
    name := "aggregator",

    mainClass in (Compile, run) := Commons.producerMainClass,
    mainClass in (Compile, packageBin) := Commons.producerMainClass,
    mainClass in assembly := Commons.producerMainClass,

    libraryDependencies ++= Dependencies.commonDependencies,
    libraryDependencies += "org.apache.kafka" % "kafka-clients"     % Dependencies.kafkaClientVersion,
    libraryDependencies += "org.twitter4j"    % "twitter4j-stream"  % Dependencies.twitter4JVersion

  ) dependsOn shared

lazy val processor = (project in file("processor")).
  settings(Commons.settings: _*).
  settings(
    name := "processor",

    libraryDependencies ++= Dependencies.commonDependencies,
    libraryDependencies += "org.apache.kafka" % "kafka-clients"     % Dependencies.kafkaClientVersion,
    libraryDependencies += "org.apache.spark" %% "spark-core"       % "2.0.0",
    libraryDependencies += "org.apache.spark" %% "spark-sql"        % "2.0.0",
    libraryDependencies += "org.apache.spark" %% "spark-streaming"  % "2.0.0",
    libraryDependencies += "org.apache.spark" %% "spark-streaming-kafka-0-10" % "2.0.0",
    libraryDependencies += "org.scalaj"       %% "scalaj-http"      % "2.3.0",
    libraryDependencies += "org.twitter4j"    % "twitter4j-stream"  % Dependencies.twitter4JVersion

  ) dependsOn shared

lazy val shared = (project in file("shared")).
  settings(Commons.settings: _*).
  settings(
    name := "shared",

    libraryDependencies ++= Dependencies.commonDependencies,
    libraryDependencies += "org.twitter4j" % "twitter4j-stream" % Dependencies.twitter4JVersion
  )

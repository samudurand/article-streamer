lazy val root = project.in(file(".")) dependsOn(aggregator) aggregate(aggregator) enablePlugins(JavaAppPackaging)

lazy val aggregator = project.dependsOn(shared) enablePlugins(JavaAppPackaging)

lazy val processor = project.dependsOn(shared) enablePlugins(JavaAppPackaging)

lazy val shared = project enablePlugins(JavaAppPackaging)

version := "1.0"

scalaVersion := "2.11.8"

// Starts the aggregator
run in Compile <<= (run in Compile in aggregator)

packageBin in Compile <<= (packageBin in Compile in aggregator)

assembly in Compile <<= (assembly in Compile in aggregator)

herokuAppName in Compile := "article-streamer-aggregator"

herokuFatJar in Compile := Some((assemblyOutputPath in assembly).value)

herokuProcessTypes in Compile := Map(
  "worker" -> "java -jar target/scala-2.11/root-assembly-1.0.jar"
)


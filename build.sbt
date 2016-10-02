
lazy val aggregator = project.dependsOn(shared)

lazy val processor = project.dependsOn(shared)

lazy val shared = project

version := "1.0"

scalaVersion := "2.11.8"


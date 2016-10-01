lazy val aggregator = project.dependsOn(shared)

lazy val processor = project.dependsOn(shared)

lazy val shared = project
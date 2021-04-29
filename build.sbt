lazy val root = (project in file(".")).settings(
  version := "0.0.1",
  name := "hello",
  scalaVersion := "2.12.8"
)

// META-INF discarding
assemblyMergeStrategy in assembly := {
  case PathList("META-INF", xs @ _*) => MergeStrategy.discard
  case x                             => MergeStrategy.first
}

artifactName := { (sv: ScalaVersion, module: ModuleID, artifact: Artifact) =>
  "skeleton" + "." + artifact.extension
}

jacocoReportSettings := JacocoReportSettings().withFormats(JacocoReportFormats.XML)

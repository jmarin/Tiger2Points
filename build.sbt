name := "Tiger2Points"

version := "0.0.1"

scalaVersion := "2.11.4"

javaOptions += "-Xmx2G"

libraryDependencies ++= {
  val akkaStreams = "1.0-M2"
  val scale = "0.0.1-SNAPSHOT"
  Seq(
    "com.typesafe.akka" %% "akka-stream-experimental" % akkaStreams,
    "scale" %% "scale-core" % scale,
    "scale" %% "scale-io" % scale,
    "scale" %% "scale-serialization" % scale
  )
}

scalariformSettings


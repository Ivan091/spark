name := "spark"
scalaVersion := "2.13.16"

Global / fork := true
Global / cancelable := true
Global / connectInput := true
Global / onChangedBuildSource := ReloadOnSourceChanges

javaOptions ++= Seq("-Xmx8G")

scalacOptions ++= Seq("-old-syntax", "-no-indent")

lazy val SparkV = "4.0.1"
lazy val CirceV = "0.14.14"

libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-core" % SparkV,
  "org.apache.spark" %% "spark-sql" % SparkV,
  "org.apache.spark" %% "spark-mllib" % SparkV,
  "org.apache.spark" %% "spark-streaming" % SparkV,
  "org.apache.spark" %% "spark-graphx" % SparkV,
  "org.typelevel" %% "cats-effect" % "3.6.3",
  "io.circe" %% "circe-generic" % CirceV,
  "io.circe" %% "circe-parser" % CirceV,
  "com.typesafe" % "config" % "1.4.5",
  "org.typelevel" %% "log4cats-slf4j" % "2.7.1",
  "ch.qos.logback" % "logback-classic" % "1.5.18" % Runtime,
  "org.slf4j" % "slf4j-simple" % "2.0.17" % Runtime,
  "org.tpolecat" %% "typename" % "1.1.0",
  "co.fs2" %% "fs2-core" % "3.12.2",
  "org.scalatest" %% "scalatest" % "3.2.19" % Test,
  "org.typelevel" %% "cats-effect-testing-scalatest" % "1.7.0" % Test,
  "org.scalatestplus" %% "mockito-4-6" % "3.2.15.0" % Test
)

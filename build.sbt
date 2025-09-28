name := "spark"
scalaVersion := "2.13.16"

Global / fork := true
Global / cancelable := true
Global / connectInput := true
Global / onChangedBuildSource := ReloadOnSourceChanges

javaOptions ++= Seq("-Xmx8G", "-Dfile.encoding=UTF-8", "-Dsun.stdout.encoding=UTF-8", "-Dsun.stderr.encoding=UTF-8")

scalacOptions ++= Seq(
  "-deprecation",
  "-feature",
  "-unchecked",
  "-explaintypes",
  "-Xlint:_",
  "-Xlint:-unused"
//  "-Wdead-code",
//  "-Wextra-implicit",
//  "-Wmacros:both",
//  "-Wnumeric-widen",
//  "-Woctal-literal",
//  "-Wself-implicit",
//  "-Wunused:_",
//  "-Wvalue-discard",
)

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
  "org.tpolecat" %% "typename" % "1.1.0",
  "co.fs2" %% "fs2-core" % "3.12.2",
  "org.scalatest" %% "scalatest" % "3.2.19" % Test,
  "org.typelevel" %% "cats-effect-testing-scalatest" % "1.7.0" % Test,
  "org.scalatestplus" %% "mockito-4-6" % "3.2.15.0" % Test,
  "org.scalacheck" %% "scalacheck" % "1.19.0"
)

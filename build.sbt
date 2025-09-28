name := "scala-with-cats"
scalaVersion := "3.4.0"

Global / fork := true
Global / cancelable := true
Global / connectInput := true
Global / onChangedBuildSource := ReloadOnSourceChanges

javaOptions ++= Seq("-Xmx8G")

scalacOptions ++= Seq("-old-syntax", "-no-indent")

libraryDependencies ++= Seq(
  "org.typelevel" %% "cats-effect" % "3.5.4",
  "org.typelevel" %% "cats-effect-testing-scalatest" % "1.5.0" % Test,
  "org.scalatest" %% "scalatest" % "3.2.18" % Test,
  "org.scalatestplus" %% "mockito-4-6" % "3.2.15.0" % Test,
  "io.circe" %% "circe-generic" % "0.14.6",
  "io.circe" %% "circe-parser" % "0.14.6",
  "com.typesafe" % "config" % "1.4.3",
  "org.typelevel" %% "log4cats-slf4j" % "2.6.0",
  "ch.qos.logback" % "logback-classic" % "1.5.3" % Runtime,
  "org.typelevel" %% "shapeless3-deriving" % "3.4.1",
  "org.tpolecat" %% "typename" % "1.1.0",
  "co.fs2" %% "fs2-core" % "3.10.0"
)

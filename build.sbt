name := """rabbitmqAkkaDemo"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  jdbc,
  cache,
  ws,
  "org.scalatestplus.play" %% "scalatestplus-play" % "1.5.1" % Test
)

resolvers += "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases"
libraryDependencies += "com.thenewmotion.akka" %% "akka-rabbitmq" % "2.3"
libraryDependencies += "com.typesafe.akka" %% "akka-remote" % "2.4.9-RC2"
name := "telegram-bot"

version := "0.1"

scalaVersion := "2.12.9"

libraryDependencies ++= Seq(
  "com.bot4s" %% "telegram-core" % "4.4.0-RC2",
  "com.softwaremill.sttp" %% "core" % "1.7.2",
  "com.softwaremill.sttp" %% "json4s" % "1.7.2",
  "org.json4s" %% "json4s-native" % "3.6.0",
  "com.typesafe.slick" %% "slick" % "3.3.1",
  "org.slf4j" % "slf4j-nop" % "1.7.26",
  "com.typesafe.slick" %% "slick-hikaricp" % "3.3.1",
  "com.h2database" % "h2" % "1.4.200",
  "org.scalatest" %% "scalatest" % "3.1.1" % "test",
  "org.scalamock" %% "scalamock" % "4.4.0" % Test
)
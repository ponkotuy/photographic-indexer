val ScalatraVersion = "3.0.0-M2"

ThisBuild / scalaVersion := "3.1.3"
ThisBuild / organization := "com.ponkotuy"
ThisBuild / scalacOptions ++= Seq("-unchecked", "-deprecation")

lazy val hello = (project in file("."))
  .settings(
    name := "Photographic Indexer",
    version := "0.1.0-SNAPSHOT",
    resolvers += "GBIF Repository" at "https://repository.gbif.org/repository/releases/",
    libraryDependencies ++= Seq(
      "org.scalatra" %% "scalatra" % ScalatraVersion,
      "org.scalatra" %% "scalatra-scalatest" % ScalatraVersion % "test",
      "ch.qos.logback" % "logback-classic" % "1.2.11" % "runtime",
      "org.eclipse.jetty" % "jetty-webapp" % "11.0.11" % "container",
      "jakarta.servlet" % "jakarta.servlet-api" % "5.0.0",
      "org.scalikejdbc" %% "scalikejdbc" % "4.0.0",
      "org.scalikejdbc" %% "scalikejdbc-syntax-support-macro" % "4.0.0",
      "mysql" % "mysql-connector-java" % "8.0.30",
      "io.circe" %% "circe-core" % "0.14.2",
      "io.circe" %% "circe-generic" % "0.14.2",
      "io.circe" %% "circe-parser" % "0.14.2",
      "com.typesafe" % "config" % "1.4.2",
      "com.drewnoakes" % "metadata-extractor" % "2.18.0",
      "org.gbif" % "gbif-parsers" % "0.59"
    )
  )

enablePlugins(ContainerPlugin)

val jettyRunner = "org.eclipse.jetty" %  "jetty-runner" % "11.0.11"

Container / containerLibs := Seq(
  jettyRunner.intransitive()
)
Container / containerMain := "org.eclipse.jetty.runner.Runner"

import com.typesafe.sbt.packager.docker._
import NativePackagerHelper._

val ScalatraVersion = "3.0.0-M2"
val CirceVersion = "0.14.3"
val defaultJOption = "--add-exports=java.desktop/sun.awt.image=ALL-UNNAMED"

ThisBuild / scalaVersion := "3.2.1"
ThisBuild / organization := "com.ponkotuy"
ThisBuild / scalacOptions ++= Seq("-unchecked", "-deprecation")
ThisBuild / javaOptions += defaultJOption

lazy val hello = (project in file("."))
  .enablePlugins(ContainerPlugin)
  .enablePlugins(JavaAppPackaging)
  .enablePlugins(DockerPlugin)
  .settings(
    name := "Photographic Indexer",
    version := "0.3.1",
    resolvers += "GBIF Repository" at "https://repository.gbif.org/repository/releases/",
    libraryDependencies ++= Seq(
      "org.scalatra" %% "scalatra" % ScalatraVersion,
      "org.scalatra" %% "scalatra-scalatest" % ScalatraVersion % "test",
      "ch.qos.logback" % "logback-classic" % "1.4.5" % "runtime",
      "org.eclipse.jetty" % "jetty-webapp" % "11.0.13" % "container;compile",
      "jakarta.servlet" % "jakarta.servlet-api" % "5.0.0",
      "org.scalikejdbc" %% "scalikejdbc" % "4.0.0",
      "org.scalikejdbc" %% "scalikejdbc-syntax-support-macro" % "4.0.0",
      "mysql" % "mysql-connector-java" % "8.0.30",
      "io.circe" %% "circe-core" % CirceVersion,
      "io.circe" %% "circe-generic" % CirceVersion,
      "io.circe" %% "circe-parser" % CirceVersion,
      "com.typesafe" % "config" % "1.4.2",
      "com.drewnoakes" % "metadata-extractor" % "2.18.0",
      "org.gbif" % "gbif-parsers" % "0.59",
      "org.apache.commons" % "commons-math3" % "3.6.1",
      "com.flickr4java" % "flickr4java" % "3.0.6"
    ),
    dockerExposedPorts ++= Seq(8080, 8080),
    dockerBaseImage := "amd64/eclipse-temurin:19-jre-jammy",
    dockerUsername := Some("ponkotuy"),
    dockerUpdateLatest := true,
    Docker / daemonUserUid := Some("1000"),
    Universal / mappings ++= directory("view/build").map { case (f, to) =>
      f -> rebase(file("build"), "view")(file(to)).get
    },
    dockerCommands ++= Cmd("ENV", "ENV_VIEW_STATIC_DIR", "view") ::
      ExecCmd("CMD", "-J" + defaultJOption) :: Nil,
    dockerEntrypoint := "bin/jetty-launcher" :: Nil
  )

val jettyRunner = "org.eclipse.jetty" %  "jetty-runner" % "11.0.13"

Container / containerLibs := Seq(
  jettyRunner.intransitive()
)
Container / containerMain := "org.eclipse.jetty.runner.Runner"

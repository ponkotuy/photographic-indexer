import com.typesafe.sbt.packager.docker.*
import NativePackagerHelper.*

val ScalatraVersion = "3.1.0"
val CirceVersion = "0.14.10"
val defaultJOption = "--add-exports=java.desktop/sun.awt.image=ALL-UNNAMED"

ThisBuild / scalaVersion := "3.3.4"
ThisBuild / organization := "com.ponkotuy"
ThisBuild / scalacOptions ++= Seq("-unchecked", "-deprecation", "-Xmax-inlines", "64", "-no-indent", "-rewrite")
ThisBuild / javaOptions += defaultJOption
Test / fork := true

val installExiftool = "apt-get update && " +
  "apt-get install exiftool -y --no-install-recommends && " +
  "apt-get -y clean && rm -rf /var/lib/apt/lists/*"

lazy val api = (project in file("."))
  .enablePlugins(ContainerPlugin)
  .enablePlugins(JavaAppPackaging)
  .enablePlugins(DockerPlugin)
  .enablePlugins(JettyPlugin)
  .settings(
    name := "Photographic Indexer",
    version := sys.env.get("VERSION").getOrElse("snapshot"),
    resolvers += "GBIF Repository" at "https://repository.gbif.org/repository/releases/",
    libraryDependencies ++= Seq(
      "org.scalatra" %% "scalatra-jakarta" % ScalatraVersion,
      "org.scalatra" %% "scalatra-json-jakarta" % ScalatraVersion,
      "org.scalatra" %% "scalatra-forms-jakarta" % ScalatraVersion,
      "org.scalatra" %% "scalatra-scalatest-jakarta" % ScalatraVersion % "test",
      "org.scalameta" %% "munit" % "1.0.3" % Test,
      "com.h2database" % "h2" % "2.3.232" % Test,
      "ch.qos.logback" % "logback-classic" % "1.5.12" % "runtime",
      "org.eclipse.jetty.ee10" % "jetty-ee10-webapp" % "12.0.16" % "container",
      "jakarta.servlet" % "jakarta.servlet-api" % "6.1.0" % "provided",
      "org.scalikejdbc" %% "scalikejdbc" % "4.3.2",
      "org.scalikejdbc" %% "scalikejdbc-syntax-support-macro" % "4.3.2",
      "com.mysql" % "mysql-connector-j" % "8.4.0",
      "io.circe" %% "circe-core" % CirceVersion,
      "io.circe" %% "circe-generic" % CirceVersion,
      "io.circe" %% "circe-parser" % CirceVersion,
      "com.typesafe" % "config" % "1.4.3",
      "org.gbif" % "gbif-parsers" % "0.59",
      "org.apache.commons" % "commons-math3" % "3.6.1",
      "com.flickr4java" % "flickr4java" % "3.0.9",
      "io.github.yskszk63" % "jnhttp-multipartformdata-bodypublisher" % "0.0.1"
    ),
    dockerExposedPorts ++= Seq(8080, 8080),
    dockerBaseImage := "amd64/eclipse-temurin:17-jre-jammy",
    dockerUsername := Some("ponkotuy"),
    dockerUpdateLatest := true,
    Docker / daemonUserUid := Some("1000"),
    Universal / mappings ++= directory("view/build").map { case (f, to) =>
      f -> rebase(file("build"), "view")(file(to)).get
    },
    dockerCommands ++= Cmd("USER", "root") ::
      Cmd("RUN", installExiftool) ::
      Cmd("USER", "1000:0") ::
      ExecCmd("CMD", "-J" + defaultJOption) :: Nil,
    dockerEntrypoint := "bin/jetty-launcher" :: Nil,
    testFrameworks += new TestFramework("munit.Framework")
  )

Jetty / containerLibs := Seq("org.eclipse.jetty.ee10" % "jetty-ee10-runner" % "12.0.10" intransitive ())
Jetty / containerMain := "org.eclipse.jetty.ee10.runner.Runner"

import com.typesafe.sbt.packager.docker.*
import com.earldouglas.sbt.war.SbtWar
import NativePackagerHelper.*

val ScalatraVersion = "3.2.1"
val CirceVersion = "0.14.17"
val JettyVersion = "12.1.15"
val defaultJOption = "--add-exports=java.desktop/sun.awt.image=ALL-UNNAMED"

ThisBuild / scalaVersion := "3.10.1"
ThisBuild / organization := "com.ponkotuy"
ThisBuild / scalacOptions ++= Seq(
  "-unchecked",
  "-deprecation",
  "-Xmax-inlines",
  "64",
  "-no-indent",
  "-rewrite",
  "-source",
  "3.4-migration"
)
ThisBuild / javaOptions += defaultJOption
Test / fork := true
Test / parallelExecution := false

val installPackages = "dnf install -y epel-release && " +
  "dnf install -y --setopt=install_weak_deps=False perl-Image-ExifTool && " +
  "dnf clean all && rm -rf /var/cache/dnf"

lazy val api = (project in file("."))
  .enablePlugins(JavaAppPackaging)
  .enablePlugins(DockerPlugin)
  .enablePlugins(SbtWar)
  .settings(
    name := "Photographic Indexer",
    version := sys.env.get("VERSION").getOrElse("snapshot"),
    resolvers += "GBIF Repository" at "https://repository.gbif.org/repository/releases/",
    libraryDependencies ++= Seq(
      "org.scalatra" %% "scalatra-jakarta" % ScalatraVersion,
      "org.scalatra" %% "scalatra-json-jakarta" % ScalatraVersion,
      "org.scalatra" %% "scalatra-forms-jakarta" % ScalatraVersion,
      "org.scalatra" %% "scalatra-scalatest-jakarta" % ScalatraVersion % "test",
      "org.scalameta" %% "munit" % "1.3.6" % Test,
      "com.h2database" % "h2" % "2.5.252" % Test,
      "ch.qos.logback" % "logback-classic" % "1.6.7" % "runtime",
      "org.eclipse.jetty.ee10" % "jetty-ee10-webapp" % JettyVersion,
      "jakarta.servlet" % "jakarta.servlet-api" % "6.1.0" % "provided",
      "org.scalikejdbc" %% "scalikejdbc" % "4.3.5",
      "org.scalikejdbc" %% "scalikejdbc-syntax-support-macro" % "4.3.5",
      "com.mysql" % "mysql-connector-j" % "26.10.0",
      "io.circe" %% "circe-core" % CirceVersion,
      "io.circe" %% "circe-generic" % CirceVersion,
      "io.circe" %% "circe-parser" % CirceVersion,
      "com.typesafe" % "config" % "1.4.10",
      "org.gbif" % "gbif-parsers" % "0.69",
      "org.apache.commons" % "commons-math3" % "3.6.1"
    ),
    dockerExposedPorts ++= Seq(8080, 8080),
    // 25.0.5.1 (Zulu 25.38.201) fixes CVE-2026-114938 (Java2Shell / Java4Shell).
    dockerBaseImage := "azul-zulu:25.38-25.0.5.1-jre-headless-almalinux10",
    dockerUsername := Some("ponkotuy"),
    dockerUpdateLatest := true,
    Docker / daemonUserUid := Some("1000"),
    Universal / mappings ++= contentOf(baseDirectory.value / "view" / "build", fileConverter.value).map { case (f, to) =>
      f -> s"view/$to"
    },
    dockerCommands ++= Cmd("USER", "root") ::
      Cmd("RUN", installPackages) ::
      Cmd("USER", "1000:0") ::
      ExecCmd("CMD", "-J" + defaultJOption) :: Nil,
    dockerEntrypoint := "bin/jetty-launcher" :: Nil,
    testFrameworks += new TestFramework("munit.Framework")
  )

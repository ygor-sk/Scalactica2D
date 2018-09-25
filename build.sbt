// shadow sbt-scalajs' crossProject and CrossType from Scala.js 0.6.x
import sbtcrossproject.CrossPlugin.autoImport.{crossProject, CrossType}

lazy val root = (project in file("."))
  .settings(
    name := "scalactica2d",
    commonSettings,
  )
  .aggregate(
    scalactica2dJs,
    scalactica2dWeb,
    scalactica2dMacro,
    scalactica2dSharedJvm,
    scalactica2dSharedJs
  )

lazy val scalactica2dWeb = (project in file("modules/scalactica2d-web"))
  .enablePlugins(PlayScala)
  .settings(
    name := "scalactica2d-web",
    commonSettings,
    pipelineStages in Assets := Seq(scalaJSPipeline),
    scalaJSProjects := Seq(scalactica2dJs),
    libraryDependencies += "com.lihaoyi" %% "autowire" % "0.2.6",
    libraryDependencies += "com.lihaoyi" %% "upickle" % "0.5.0",
  )
  .dependsOn(scalactica2dMacro, scalactica2dSharedJvm)

lazy val scalactica2dJs = (project in file("modules/scalactica2d-js"))
  .enablePlugins(ScalaJSPlugin)
  .settings(
    name := "scalactica2d-js",
    commonSettings,
    libraryDependencies += "org.scala-js" %%% "scalajs-dom" % "0.9.5",
    libraryDependencies += "org.querki" %%% "jquery-facade" % "1.2",
    libraryDependencies += "com.lihaoyi" %%% "autowire" % "0.2.6",
    libraryDependencies += "com.lihaoyi" %%% "upickle" % "0.5.0",
  )
  .dependsOn(scalactica2dMacro, scalactica2dSharedJs)

lazy val scalactica2dShared = crossProject(JSPlatform, JVMPlatform)
  .crossType(CrossType.Pure)
  .in(file("modules/scalactica2d-shared"))
  .settings(
    name := "scalactica2d-shared",
    commonSettings,
  )

lazy val scalactica2dSharedJvm = scalactica2dShared.jvm
lazy val scalactica2dSharedJs = scalactica2dShared.js

lazy val scalactica2dMacro = (project in file("modules/scalactica2d-macro"))
  .settings(
    name := "scalactica2d-macro",
    commonSettings,
  )

lazy val commonSettings = Seq(
  organization := "sk.ygor",
  version := "1.0-SNAPSHOT",
  scalaVersion := "2.12.6",
  libraryDependencies += "com.softwaremill.macwire" %% "macros" % "2.3.1" % "provided",
  libraryDependencies += "com.softwaremill.macwire" %% "util" % "2.3.1",
)

addCommandAlias("runWeb", "scalactica2dWeb/run")
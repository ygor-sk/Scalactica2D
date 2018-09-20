// shadow sbt-scalajs' crossProject and CrossType from Scala.js 0.6.x
import sbtcrossproject.CrossPlugin.autoImport.{crossProject, CrossType}

lazy val root = (project in file("."))
  .settings(
    name := "space2d",
    commonSettings,
  )
  .aggregate(
    space2dJs,
    space2dWeb,
    space2dMacro,
    space2dSharedJvm,
    space2dSharedJs
  )

lazy val space2dWeb = (project in file("modules/space2d-web"))
  .enablePlugins(PlayScala)
  .settings(
    name := "space2d-web",
    commonSettings,
    pipelineStages in Assets := Seq(scalaJSPipeline),
    scalaJSProjects := Seq(space2dJs),
    libraryDependencies += "com.lihaoyi" %% "autowire" % "0.2.6",
    libraryDependencies += "com.lihaoyi" %% "upickle" % "0.5.0",
  )
  .dependsOn(space2dMacro, space2dSharedJvm)

lazy val space2dJs = (project in file("modules/space2d-js"))
  .enablePlugins(ScalaJSPlugin)
  .settings(
    name := "space2d-js",
    commonSettings,
    libraryDependencies += "org.scala-js" %%% "scalajs-dom" % "0.9.5",
    libraryDependencies += "org.querki" %%% "jquery-facade" % "1.2",
    libraryDependencies += "com.lihaoyi" %%% "autowire" % "0.2.6",
    libraryDependencies += "com.lihaoyi" %%% "upickle" % "0.5.0",
  )
  .dependsOn(space2dMacro, space2dSharedJs)

lazy val space2dShared = crossProject(JSPlatform, JVMPlatform)
  .crossType(CrossType.Pure)
  .in(file("modules/space2d-shared"))
  .settings(
    name := "space2d-shared",
    commonSettings,
  )

lazy val space2dSharedJvm = space2dShared.jvm
lazy val space2dSharedJs = space2dShared.js

lazy val space2dMacro = (project in file("modules/space2d-macro"))
  .settings(
    name := "space2d-macro",
    commonSettings,
  )

lazy val commonSettings = Seq(
  organization := "sk.ygor",
  version := "1.0-SNAPSHOT",
  scalaVersion := "2.12.6",
  libraryDependencies += "com.softwaremill.macwire" %% "macros" % "2.3.1" % "provided",
  libraryDependencies += "com.softwaremill.macwire" %% "util" % "2.3.1",
)

addCommandAlias("runWeb", "space2dWeb/run")
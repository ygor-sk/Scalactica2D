lazy val root = (project in file("."))
  .settings(
    name := "space2d",
    commonSettings,
  )
  .aggregate(
    space2dJs,
    space2dWeb
  )

lazy val space2dWeb = (project in file("modules/space2d-web"))
  .enablePlugins(PlayScala)
  .settings(
    name := "space2d-web",
    commonSettings,
    pipelineStages in Assets := Seq(scalaJSPipeline),
    scalaJSProjects := Seq(space2dJs),
  )
  .dependsOn(
    space2dJs
  )

lazy val space2dJs = (project in file("modules/space2d-js"))
  .enablePlugins(ScalaJSPlugin)
  .settings(
    name := "space2d-js",
    commonSettings,
    libraryDependencies += "org.scala-js" %%% "scalajs-dom" % "0.9.5",
//    libraryDependencies += "org.querki" %%% "jquery-facade" % "1.2",
    scalaJSUseMainModuleInitializer := true
  )

lazy val commonSettings = Seq(
  organization := "sk.ygor",
  version := "1.0-SNAPSHOT",
  scalaVersion := "2.12.6",
  libraryDependencies += "com.softwaremill.macwire" %% "macros" % "2.3.1" % "provided",
  libraryDependencies += "com.softwaremill.macwire" %% "util" % "2.3.1",
)

addCommandAlias("runWeb", "space2dWeb/run")
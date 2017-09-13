organization in ThisBuild := "io.estatico"

lazy val effect = project.in(file("."))
  .aggregate(core, scalaz7)

lazy val core = module("core")

lazy val scalaz7 = module("scalaz7")
  .dependsOn(core)
  .settings(
    libraryDependencies += "org.scalaz" %% "scalaz-concurrent" % "7.2.15"
  )

lazy val defaultScalacOptions = scalacOptions ++= Seq(
  "-Xfatal-warnings",
  "-unchecked",
  "-feature",
  "-deprecation",
  "-language:higherKinds",
  "-language:implicitConversions"
)

lazy val defaultTestDependencies = libraryDependencies ++= Seq(
  "org.scalacheck" %% "scalacheck" % "1.13.4",
  "org.scalatest" %% "scalatest" % "3.0.0"
).map(_ % "test")

def module(path: String) = {
  // Convert path from lisp-case to camelCase
  val id = path.split("-").reduce(_ + _.capitalize)
  // Convert path from list-case to "space case"
  val docName = path.replace('-', ' ')
  // Set default and module-specific settings.
  applyDefaultSettings(Project(id, file(path))).settings(
    name := "Effect " + docName,
    moduleName := "effect-" + path,
    description := "effect" + docName
  )
}

def applyDefaultSettings(project: Project) = project.settings(
  defaultScalacOptions,
  defaultTestDependencies
)

organization in ThisBuild := "io.estatico"

lazy val effect = project.in(file("."))
  .aggregate(core, scalaz7)

lazy val core = module("core")

lazy val coreTests = module("coreTests")
  .dependsOn(core, laws % "test")

lazy val laws = module("laws")
  .dependsOn(core)
  .settings(
    libraryDependencies ++= Seq(
      "org.typelevel" %% "cats-laws" % catsVersion,
      "org.typelevel" %% "discipline" % disciplineVersion
    )
  )

lazy val scalaz7 = module("scalaz7")
  .dependsOn(core, laws % "test")
  .settings(
    libraryDependencies ++= Seq(
      "org.scalaz" %% "scalaz-concurrent",
      "org.scalaz" %% "scalaz-core"
    ).map(_ % scalaz7Version)
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
  "org.scalacheck" %% "scalacheck" % scalacheckVersion,
  "org.scalatest" %% "scalatest" % scalatestVersion
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

lazy val catsVersion = "0.9.0"
lazy val disciplineVersion = "0.8"
lazy val scalacheckVersion = "1.13.5"
lazy val scalatestVersion = "3.0.3"
lazy val scalaz7Version = "7.2.15"

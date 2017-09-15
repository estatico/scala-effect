import ReleaseTransformations._

organization in ThisBuild := "io.estatico"

lazy val effect = project.in(file("."))
  .settings(noPublishSettings)
  .aggregate(core, laws, coreTests, scalaz7)

lazy val core = baseModule("core")

lazy val laws = baseModule("laws")
  .dependsOn(core) .settings(
    libraryDependencies ++= defaultTestDependencies,
    libraryDependencies ++= Seq(
      "org.typelevel" %% "cats-laws" % catsVersion,
      "org.typelevel" %% "discipline" % disciplineVersion
    )
  )

// Needed so core tests can depend on laws, which can't happen in core since laws depends on core.
lazy val coreTests = module("core-tests")
  .settings(noPublishSettings)

lazy val scalaz7 = module("scalaz7")
  .dependsOn(core, laws % "test")
  .settings(
    libraryDependencies ++= Seq(
      "org.scalaz" %% "scalaz-concurrent",
      "org.scalaz" %% "scalaz-core"
    ).map(_ % scalaz7Version)
  )

lazy val defaultScalacOptions = Seq(
  "-Xfatal-warnings",
  "-unchecked",
  "-feature",
  "-deprecation",
  "-language:higherKinds",
  "-language:implicitConversions"
)

lazy val defaultTestDependencies = Seq(
  "org.scalacheck" %% "scalacheck" % scalacheckVersion,
  "org.scalatest" %% "scalatest" % scalatestVersion
)

/** Construct a module that with default settings but does not depend on core or laws. */
def baseModule(path: String) = {
  // Convert path from lisp-case to camelCase
  val id = path.split("-").reduce(_ + _.capitalize)
  // Convert path from list-case to "space case"
  val docName = path.replace('-', ' ')
  // Set default and module-specific settings.
  applyDefaultSettings(Project(id, file(path))).settings(
    name := "Effect " + docName,
    moduleName := "effect-" + path,
    description := "effect " + docName,
    defaultPublishSettings
  )
}

/** Same as `baseModule` except depends on core and laws (laws only for tests). */
def module(path: String) = baseModule(path).dependsOn(core, laws % "test")

def applyDefaultSettings(project: Project) = project.settings(
  scalacOptions ++= defaultScalacOptions,
  libraryDependencies ++= defaultTestDependencies.map(_ % "test")
)

lazy val catsVersion = "0.9.0"
lazy val disciplineVersion = "0.8"
lazy val scalacheckVersion = "1.13.5"
lazy val scalatestVersion = "3.0.3"
lazy val scalaz7Version = "7.2.15"

// Publish settings

lazy val defaultPublishSettings = Seq(
  releaseCrossBuild := true,
  releasePublishArtifactsAction := PgpKeys.publishSigned.value,
  releaseProcess := Seq[ReleaseStep](
    checkSnapshotDependencies,
    inquireVersions,
    runClean,
    runTest,
    setReleaseVersion,
    commitReleaseVersion,
    tagRelease,
    publishArtifacts,
    setNextVersion,
    commitNextVersion,
    ReleaseStep(action = Command.process("sonatypeReleaseAll", _)),
    pushChanges
  ),
  homepage := Some(url("https://github.com/estatico/scala-effect")),
  licenses := Seq("Apache 2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0")),
  publishMavenStyle := true,
  publishArtifact in Test := false,
  pomIncludeRepository := { _ => false },
  publishTo := {
    val nexus = "https://oss.sonatype.org/"
    if (isSnapshot.value)
      Some("snapshots" at nexus + "content/repositories/snapshots")
    else
      Some("releases"  at nexus + "service/local/staging/deploy/maven2")
  },
  scmInfo := Some(
    ScmInfo(
      url("https://github.com/estatico/scala-effect"),
      "scm:git:git@github.com:estatico/scala-effect.git"
    )
  ),
  developers := List(
    Developer("caryrobbins", "Cary Robbins", "carymrobbins@gmail.com", url("http://caryrobbins.com"))
  )
)

lazy val noPublishSettings = Seq(
  publish := (),
  publishLocal := (),
  publishArtifact := false
)

credentials ++= (
  for {
    username <- Option(System.getenv().get("SONATYPE_USERNAME"))
    password <- Option(System.getenv().get("SONATYPE_PASSWORD"))
  } yield Credentials(
    "Sonatype Nexus Repository Manager",
    "oss.sonatype.org",
    username,
    password
  )
  ).toSeq

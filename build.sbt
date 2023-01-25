ThisBuild / organization := "one.estrondo"
ThisBuild / scalaVersion := "3.2.1"
ThisBuild / version      := "0.0.1-SNAPSHOT"
ThisBuild / isSnapshot   := true

ThisBuild / scalacOptions ++= Seq(
  "-explain"
)

publishMavenStyle                  := true
ThisBuild / sonatypeCredentialHost := "s01.oss.sonatype.org"
ThisBuild / publishTo              := sonatypePublishToBundle.value
ThisBuild / licenses               := Seq("GPLv3" -> url("https://www.gnu.org/licenses/gpl-3.0.txt"))
ThisBuild / homepage               := Some(url("https://github.com/estrondo/farango"))
ThisBuild / scmInfo                := Some(
  ScmInfo(
    url("https://github.com/estrondo/farango"),
    "scm:git@github.com:estrondo/farango.git"
  )
)

ThisBuild / developers             := List(
  Developer(
    id = "rthoth",
    name = "Ronaldo Silva",
    email = "ronaldo.asilva@gmail.com",
    url = url("https://github.com/rthoth")
  )
)

lazy val root = (project in file("."))
  .settings(
    name                                   := "farango-root",
    publishTo                              := sonatypePublishToBundle.value,
    Compile / packageBin / publishArtifact := false,
    Compile / packageDoc / publishArtifact := false,
    Compile / packageSrc / publishArtifact := false
  )
  .aggregate(farango, zarango)

lazy val farango = (project in file("farango"))
  .settings(
    name := "farango",
    libraryDependencies ++= Seq(
      "com.arangodb"                  % "arangodb-java-driver"          % "6.19.0",
      "com.arangodb"                  % "jackson-dataformat-velocypack" % "3.0.1",
      "com.fasterxml.jackson.module" %% "jackson-module-scala"          % "2.13.4"
    )
  )

lazy val zarango = (project in file("zarango"))
  .settings(
    name := "zarango",
    libraryDependencies ++= Seq(
      "dev.zio" %% "zio"          % "2.0.5",
      "dev.zio" %% "zio-streams"  % "2.0.5",
      "dev.zio" %% "zio-test"     % "2.0.5" % Test,
      "dev.zio" %% "zio-test-sbt" % "2.0.5" % Test
    )
  )
  .dependsOn(farango)

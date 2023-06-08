ThisBuild / organization := "one.estrondo"
ThisBuild / scalaVersion := "3.3.0"
ThisBuild / version      := "0.0.0-SNAPSHOT"
ThisBuild / Test / fork  := true

ThisBuild / scalacOptions ++= Seq(
  "-Wunused:all",
  "-explain",
  "-deprecation",
  "-unchecked"
)

lazy val root = (project in file("."))
  .settings(
    name := "farango",
    libraryDependencies ++= Seq(
      Dependencies.ArangoDBDriver
    ).flatten
  )

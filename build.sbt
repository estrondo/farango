ThisBuild / organization := "one.estrondo"
ThisBuild / scalaVersion := "3.3.0"
ThisBuild / version      := "0.0.1"
ThisBuild / Test / fork  := true

ThisBuild / scalacOptions ++= Seq(
  "-Wunused:all",
  "-explain",
  "-deprecation",
  "-unchecked",
  "-feature"
)

ThisBuild / autoAPIMappings := true
ThisBuild / apiURL          := Some(url("https://javadoc.io/doc"))

lazy val root = (project in file("."))
  .settings(
    name           := "farango-root",
    publish / skip := true
  )
  .aggregate(
    core,
    zio,
    it,
    ducktape,
    catsEffect,
    zioIt,
    catsEffectIt,
    examples
  )

lazy val core = (project in file("core"))
  .settings(
    name      := "farango",
    publishTo := sonatypePublishToBundle.value,
    libraryDependencies ++= Seq(
      Dependencies.ArangoDBDriver,
      Dependencies.JacksonScalaModule,
      Dependencies.JacksonJavaModules,
      Dependencies.ScalaTest,
      Dependencies.ScalatestMockito
    ).flatten
  )

lazy val it = (project in file("it"))
  .settings(
    name           := "farango-it-test",
    publish / skip := true,
    libraryDependencies ++= Seq(
      Dependencies.TestcontainersScala,
      Dependencies.Logging,
      Dependencies.JacksonScalaModule
    ).flatten
  )
  .dependsOn(
    core % "test->test"
  )

lazy val zio = (project in file("zio"))
  .settings(
    name      := "farango-zio",
    publishTo := sonatypePublishToBundle.value,
    libraryDependencies ++= Seq(
      Dependencies.ZIO
    ).flatten
  )
  .dependsOn(
    core,
    core % "test->test"
  )

lazy val catsEffect = (project in file("cats-effect"))
  .settings(
    name      := "farango-cats-effect",
    publishTo := sonatypePublishToBundle.value,
    libraryDependencies ++= Seq(
      Dependencies.CatsEffect
    ).flatten
  )
  .dependsOn(
    core,
    core % "test->test"
  )

lazy val zioIt = (project in file("zio-it"))
  .settings(
    name           := "farango-zio-it",
    publish / skip := true
  )
  .dependsOn(
    zio,
    zio % "test->test",
    it  % "test->test"
  )

lazy val ducktape = (project in file("ducktape"))
  .settings(
    name      := "farango-ducktape",
    publishTo := sonatypePublishToBundle.value,
    libraryDependencies ++= Seq(
      Dependencies.DuckTape
    ).flatten
  )
  .dependsOn(
    core,
    core % "test->test"
  )

lazy val catsEffectIt = (project in file("cats-effect-it"))
  .settings(
    name           := "farango-cats-effect-it",
    publish / skip := true
  )
  .dependsOn(
    catsEffect,
    catsEffect % "test->test",
    it         % "test->test"
  )

lazy val examples = (project in file("examples"))
  .settings(
    name           := "farango-examples",
    publish / skip := true
  )
  .dependsOn(
    core,
    zio,
    catsEffect,
    ducktape
  )

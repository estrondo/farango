import sbt._
//noinspection TypeAnnotation
object Dependencies {

  val ArangoDBDriver = Seq(
    "com.arangodb" % "arangodb-java-driver" % Version.ArangoDBDriver
  )

  val CatsEffect = Seq(
    "org.typelevel" %% "cats-effect" % Version.CatsEffect,
    "co.fs2"        %% "fs2-core"    % Version.FS2
  )

  val Logging = Seq(
    "org.slf4j" % "slf4j-reload4j" % Version.Reload4j
  )

  val JacksonScalaModule = Seq(
    "com.fasterxml.jackson.module" %% "jackson-module-scala" % Version.Jackson
  )

  val DuckTape = Seq(
    "io.github.arainko" %% "ducktape" % Version.DuckTape
  )

  val JacksonJavaModules = Seq(
    "com.fasterxml.jackson.datatype" % "jackson-datatype-jsr310"        % Version.Jackson,
    "com.fasterxml.jackson.module"   % "jackson-module-parameter-names" % Version.Jackson,
    "com.fasterxml.jackson.datatype" % "jackson-datatype-jdk8"          % Version.Jackson
  )

  val ScalaTest = Seq(
    "org.scalatest" %% "scalatest" % Version.ScalaTest % Test
  )

  val ScalatestMockito = Seq(
    "org.scalatestplus" %% "mockito-4-11" % Version.ScalatestMockito % Test
  )

  val TestcontainersScala = Seq(
    "com.dimafeng" %% "testcontainers-scala-scalatest" % Version.TestcontainersScala % Test
  )

  val ZIO = Seq(
    "dev.zio" %% "zio"         % Version.ZIO,
    "dev.zio" %% "zio-streams" % Version.ZIO
  )

  object Version {
    val ArangoDBDriver      = "7.1.0"
    val CatsEffect          = "3.5.0"
    val DuckTape            = "0.1.8"
    val FS2                 = "3.7.0"
    val Reload4j            = "2.0.7"
    val ScalaTest           = "3.2.16"
    val ScalatestMockito    = "3.2.16.0"
    val TestcontainersScala = "0.40.15"
    val Jackson             = "2.15.2"
    val ZIO                 = "2.0.15"
  }
}

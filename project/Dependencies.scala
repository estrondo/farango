import sbt._
//noinspection TypeAnnotation
object Dependencies {

  val ArangoDBDriver = Seq(
    "com.arangodb" % "arangodb-java-driver" % Version.ArangoDBDriver
  )

  val Logging = Seq(
    "org.slf4j" % "slf4j-reload4j" % Version.Reload4j
  )

  val JacksonScalaModule = Seq(
    "com.fasterxml.jackson.module" % "jackson-module-scala_2.13" % "2.15.2"
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
    "dev.zio" %% "zio" % Version.ZIO
  )

  object Version {
    val ArangoDBDriver      = "7.1.0"
    val Reload4j            = "2.0.7"
    val ScalaTest           = "3.2.16"
    val ScalatestMockito    = "3.2.16.0"
    val TestcontainersScala = "0.40.15"
    val ZIO                 = "2.0.15"
  }
}

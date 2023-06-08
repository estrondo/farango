import sbt._
//noinspection TypeAnnotation
object Dependencies {

  val ArangoDBDriver = Seq(
    "com.arangodb" % "arangodb-java-driver" % Version.ArangoDBDriver
  )

  object Version {
    val ArangoDBDriver = "7.1.0"
  }
}

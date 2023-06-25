publishMavenStyle                  := true
ThisBuild / sonatypeCredentialHost := "s01.oss.sonatype.org"
ThisBuild / publishTo              := sonatypePublishToBundle.value
ThisBuild / licenses               := Seq("GPLv3" -> url("https://www.gnu.org/licenses/gpl-3.0.txt"))
ThisBuild / homepage               := Some(url("https://github.com/estrondo/farango"))

ThisBuild / scmInfo := Some(
  ScmInfo(
    url("https://github.com/estrondo/farango"),
    "scm:git@github.com:estrondo/farango.git"
  )
)

ThisBuild / developers := List(
  Developer(
    id = "rthoth",
    name = "Ronaldo Silva",
    email = "ronaldo.asilva@gmail.com",
    url = url("https://github.com/rthoth")
  )
)

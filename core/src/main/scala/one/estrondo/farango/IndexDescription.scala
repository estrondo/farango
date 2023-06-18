package one.estrondo.farango

import com.arangodb.model.GeoIndexOptions

sealed trait IndexDescription

object IndexDescription:

  case class Geo(val fields: Seq[String], options: GeoIndexOptions)

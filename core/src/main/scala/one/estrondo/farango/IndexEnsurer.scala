package one.estrondo.farango

import com.arangodb.ArangoCollection
import com.arangodb.entity.IndexEntity
import com.arangodb.entity.InvertedIndexEntity
import com.arangodb.model.GeoIndexOptions
import scala.jdk.CollectionConverters.IterableHasAsJava
import scala.util.Try

sealed trait IndexEnsurer:

  def apply(arango: ArangoCollection): Try[IndexEntity | InvertedIndexEntity]

object IndexEnsurer:

  def geoIndex(fields: Seq[String], options: GeoIndexOptions): IndexEnsurer =
    new IndexEnsurer:
      override def apply(collection: ArangoCollection): Try[IndexEntity | InvertedIndexEntity] = Try {
        collection.ensureGeoIndex(fields.asJava, options)
      }

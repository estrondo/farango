package one.estrondo.farango

import com.arangodb.ArangoCollection
import com.arangodb.entity.IndexEntity
import com.arangodb.entity.InvertedIndexEntity
import com.arangodb.model.GeoIndexOptions
import scala.jdk.CollectionConverters.IterableHasAsJava

sealed trait IndexEnsurer:

  def apply[F[_]: Effect](arango: ArangoCollection): F[IndexEntity | InvertedIndexEntity]

object IndexEnsurer:

  def geoIndex(fields: Seq[String], options: GeoIndexOptions): IndexEnsurer =
    new IndexEnsurer:
      override def apply[F[_]: Effect](arango: ArangoCollection): F[IndexEntity | InvertedIndexEntity] =
        Effect[F].attemptBlocking {
          arango.ensureGeoIndex(fields.asJava, options)
        }

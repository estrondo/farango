package one.estrondo.farango.sync.collection

import com.arangodb.ArangoCollection
import one.estrondo.farango.IndexDescription
import scala.jdk.CollectionConverters.SeqHasAsJava

object SyncIndexEnsurer:

  def apply(index: IndexDescription, collection: ArangoCollection): String =
    index match
      case description: IndexDescription.Geo => geo(description, collection)

  private def geo(description: IndexDescription.Geo, collection: ArangoCollection): String =
    collection.ensureGeoIndex(description.fields.asJava, description.options).getId

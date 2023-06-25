package one.estrondo.farango.sync.collection

import com.arangodb.ArangoCollection
import one.estrondo.farango.IndexDescription
import scala.jdk.CollectionConverters.SeqHasAsJava

object SyncIndexEnsurer:

  def apply(index: IndexDescription, collection: ArangoCollection): String =
    index match
      case IndexDescription.Geo(fields, options) =>
        collection.ensureGeoIndex(fields.asJava, options).getId

      case IndexDescription.Fulltext(fields, options) =>
        collection.ensureFulltextIndex(fields.asJava, options).getId

      case IndexDescription.Ttl(fields, options) =>
        collection.ensureTtlIndex(fields.asJava, options).getId

      case IndexDescription.Inverted(options) =>
        collection.ensureInvertedIndex(options).getId

      case IndexDescription.Persistent(fields, options) =>
        collection.ensurePersistentIndex(fields.asJava, options).getId

      case IndexDescription.ZKD(fields, options) =>
        collection.ensureZKDIndex(fields.asJava, options).getId

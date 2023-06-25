package one.estrondo.farango

import com.arangodb.model.FulltextIndexOptions
import com.arangodb.model.GeoIndexOptions
import com.arangodb.model.InvertedIndexOptions
import com.arangodb.model.PersistentIndexOptions
import com.arangodb.model.TtlIndexOptions
import com.arangodb.model.ZKDIndexOptions

sealed trait IndexDescription

object IndexDescription:

  case class Geo(fields: Seq[String], options: GeoIndexOptions = GeoIndexOptions()) extends IndexDescription

  @deprecated
  case class Fulltext(fields: Seq[String], options: FulltextIndexOptions = FulltextIndexOptions())
      extends IndexDescription

  case class Inverted(options: InvertedIndexOptions = InvertedIndexOptions()) extends IndexDescription

  case class Persistent(fields: Seq[String], options: PersistentIndexOptions = PersistentIndexOptions())
      extends IndexDescription

  case class Ttl(fields: Seq[String], options: TtlIndexOptions = TtlIndexOptions()) extends IndexDescription

  case class ZKD(fields: Seq[String], options: ZKDIndexOptions = ZKDIndexOptions()) extends IndexDescription

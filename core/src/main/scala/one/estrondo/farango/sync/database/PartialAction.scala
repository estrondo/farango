package one.estrondo.farango.sync.database

import com.arangodb.ArangoDatabase
import com.arangodb.model.AqlQueryOptions
import java.util
import java.util.stream
import one.estrondo.farango.database.PartialQuery
import one.estrondo.farango.sync.SyncComposed
import one.estrondo.farango.typeOf
import scala.reflect.ClassTag
import scala.util.Try

class SyncPartialQuery[A](arango: ArangoDatabase) extends PartialQuery[A], SyncComposed:
  override protected def search(query: String, bindVars: util.Map[String, Object], options: AqlQueryOptions)(using
      ClassTag[A]
  ): Try[stream.Stream[A]] =
    Try(arango.query(query, typeOf[A], bindVars, options).stream())

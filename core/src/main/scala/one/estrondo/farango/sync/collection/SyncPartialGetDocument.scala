package one.estrondo.farango.sync.collection

import com.arangodb.ArangoCollection
import com.arangodb.model.DocumentReadOptions
import one.estrondo.farango.collection.PartialGetDocument
import one.estrondo.farango.sync.SyncComposed
import one.estrondo.farango.typeOf
import scala.reflect.ClassTag
import scala.util.Try

class SyncPartialGetDocument[A, R](arango: ArangoCollection) extends PartialGetDocument[A, R], SyncComposed:

  override protected def get(key: String, options: DocumentReadOptions)(using ClassTag[A]): Try[Option[A]] =
    Try(Option(arango.getDocument(key, typeOf[A], options)))

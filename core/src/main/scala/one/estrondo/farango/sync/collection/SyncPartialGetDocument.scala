package one.estrondo.farango.sync.collection

import com.arangodb.ArangoCollection
import com.arangodb.model.DocumentReadOptions
import one.estrondo.farango.collection.PartialGetDocument
import one.estrondo.farango.sync.SyncComposed
import scala.util.Try

class SyncPartialGetDocument[A, R](arango: ArangoCollection) extends PartialGetDocument[A, R], SyncComposed:

  override protected def get(key: String, options: DocumentReadOptions, returnType: Class[A]): Try[Option[A]] =
    Try(Option(arango.getDocument(key, returnType, options)))

package one.estrondo.farango.sync.collection

import com.arangodb.ArangoCollection
import com.arangodb.entity.DocumentDeleteEntity
import com.arangodb.model.DocumentDeleteOptions
import one.estrondo.farango.collection.PartialDeleteDocument
import one.estrondo.farango.sync.SyncComposed
import scala.util.Try

class SyncPartialDeleteDocument[A, R](arango: ArangoCollection) extends PartialDeleteDocument[A, R], SyncComposed:

  override protected def remove(
      key: String,
      options: DocumentDeleteOptions,
      returnType: Class[A]
  ): Try[DocumentDeleteEntity[A]] = Try {
    arango.deleteDocument(key, options, returnType)
  }

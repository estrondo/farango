package one.estrondo.farango.sync.collection

import com.arangodb.ArangoCollection
import com.arangodb.entity.DocumentUpdateEntity
import com.arangodb.model.DocumentUpdateOptions
import one.estrondo.farango.collection.PartialUpdateDocument
import one.estrondo.farango.sync.SyncComposed
import scala.util.Try

class SyncPartialUpdateDocument[A, U, R](arango: ArangoCollection) extends PartialUpdateDocument[A, U, R], SyncComposed:

  override protected def update(
      key: String,
      value: U,
      options: DocumentUpdateOptions,
      returnType: Class[A]
  ): Try[DocumentUpdateEntity[A]] = Try {
    arango.updateDocument(key, value, options, returnType)
  }

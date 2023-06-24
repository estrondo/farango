package one.estrondo.farango.sync.collection

import com.arangodb.ArangoCollection
import com.arangodb.entity.DocumentCreateEntity
import com.arangodb.model.DocumentCreateOptions
import one.estrondo.farango.collection.PartialInsertDocument
import one.estrondo.farango.sync.SyncComposed
import scala.util.Try

class SyncPartialInsertDocument[A, R](arango: ArangoCollection) extends PartialInsertDocument[A, R], SyncComposed:

  override protected def insert(
      document: A,
      options: DocumentCreateOptions,
      returnType: Class[A]
  ): Try[DocumentCreateEntity[A]] =
    Try(arango.insertDocument(document, options, returnType))

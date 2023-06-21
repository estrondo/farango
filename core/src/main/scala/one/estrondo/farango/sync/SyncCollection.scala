package one.estrondo.farango.sync

import com.arangodb.ArangoCollection
import com.arangodb.model.CollectionCreateOptions
import one.estrondo.farango.Collection
import one.estrondo.farango.IndexDescription
import one.estrondo.farango.collection.PartialDeleteDocument
import one.estrondo.farango.collection.PartialGetDocument
import one.estrondo.farango.collection.PartialInsertDocument
import one.estrondo.farango.collection.PartialUpdateDocument
import one.estrondo.farango.sync.collection.SyncIndexEnsurer
import one.estrondo.farango.sync.collection.SyncPartialDeleteDocument
import one.estrondo.farango.sync.collection.SyncPartialGetDocument
import one.estrondo.farango.sync.collection.SyncPartialInsertDocument
import one.estrondo.farango.sync.collection.SyncPartialUpdateDocument
import scala.util.Try

trait SyncCollection extends Collection, SyncComposed:

  override type CollectionRep = SyncCollection

  def arango: ArangoCollection

object SyncCollection:

  def apply(
      database: SyncDatabase,
      name: String,
      indexes: Seq[IndexDescription],
      options: CollectionCreateOptions
  ): SyncCollection =
    Impl(database, name, indexes, options)

  private class Impl(
      val database: SyncDatabase,
      name: String,
      indexes: Seq[IndexDescription],
      options: CollectionCreateOptions
  ) extends SyncCollection:

    override val arango: ArangoCollection = database.arango.collection(name)

    override def deleteDocument[A, R]: PartialDeleteDocument[A, R] =
      SyncPartialDeleteDocument(arango)

    override def getDocument[A, R]: PartialGetDocument[A, R] =
      SyncPartialGetDocument(arango)

    override def insertDocument[A, R]: PartialInsertDocument[A, R] =
      SyncPartialInsertDocument(arango)

    override def updateDocument[A, U, R]: PartialUpdateDocument[A, U, R] =
      SyncPartialUpdateDocument(arango)

    override protected def _create(): Try[SyncCollection] =
      for root <- database.root yield
        val collection = root.collection(name)
        collection.create(options)
        for index <- indexes do SyncIndexEnsurer(index, collection)
        this

package one.estrondo.farango.sync

import com.arangodb.ArangoCollection
import com.arangodb.model.CollectionCreateOptions
import one.estrondo.farango.Collection
import one.estrondo.farango.IndexDescription
import one.estrondo.farango.collection.PartialGetDocument
import one.estrondo.farango.collection.PartialInsertDocument
import one.estrondo.farango.sync.collection.SyncIndexEnsurer
import one.estrondo.farango.sync.collection.SyncPartialGetDocument
import one.estrondo.farango.sync.collection.SyncPartialInsertDocument
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

    override def getDocument[A]: PartialGetDocument[A] =
      SyncPartialGetDocument(arango)

    override def insertDocument[A]: PartialInsertDocument[A] =
      SyncPartialInsertDocument(arango)

    override protected def _create(): Try[SyncCollection] =
      for root <- database.root yield
        val collection = root.collection(name)
        collection.create(options)
        for index <- indexes do SyncIndexEnsurer(index, collection)
        this

package one.estrondo.farango

import com.arangodb.ArangoCollection
import com.arangodb.model.CollectionCreateOptions
import one.estrondo.farango.collection.PartiallyAppliedDeleteDocument
import one.estrondo.farango.collection.PartiallyAppliedGetDocument
import one.estrondo.farango.collection.PartiallyAppliedInsertDocument
import one.estrondo.farango.collection.PartiallyAppliedUpdateDocument
import scala.util.Try

trait Collection:

  def arango: ArangoCollection

  def database: Database

  def name: String

  def create[F[_]: Effect](): F[Collection] = Effect[F].attemptBlockingTry(tryCreate())

  /** @tparam S The Stored Document's type. */
  // noinspection MutatorLikeMethodIsParameterless
  def deleteDocument[S]: PartiallyAppliedDeleteDocument[S] = PartiallyAppliedDeleteDocument(arango)

  /** @tparam S
    *   The Stored Document's type.
    */
  def getDocument[S]: PartiallyAppliedGetDocument[S] = PartiallyAppliedGetDocument(arango)

  /** @tparam S The Stored Document's type. */
  // noinspection MutatorLikeMethodIsParameterless
  def insertDocument[S]: PartiallyAppliedInsertDocument[S] = PartiallyAppliedInsertDocument(arango)

  /** @tparam S
    *   The Stored Document's type.
    * @tparam U
    *   The type which is used to update the document in collection.
    */
  // noinspection MutatorLikeMethodIsParameterless
  def updateDocument[S, U]: PartiallyAppliedUpdateDocument[S, U] = PartiallyAppliedUpdateDocument(arango)

  def tryCreate(): Try[Collection]

object Collection:

  def apply(
      database: Database,
      name: String,
      indexes: Seq[IndexEnsurer] = Nil,
      options: CollectionCreateOptions = CollectionCreateOptions()
  ): Collection =
    Impl(database, name, options, indexes)

  private class Impl(
      val database: Database,
      val name: String,
      options: CollectionCreateOptions,
      indexes: Seq[IndexEnsurer]
  ) extends Collection:

    val arango: ArangoCollection = database.arango.collection(name)

    override def tryCreate(): Try[Collection] =
      for root <- database.root yield
        val collection = root.collection(name)
        collection.create(options)
        for ensurer <- indexes do ensurer(collection)
        this

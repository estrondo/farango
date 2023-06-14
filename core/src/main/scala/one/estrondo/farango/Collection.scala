package one.estrondo.farango

import com.arangodb.ArangoCollection
import com.arangodb.entity.IndexEntity
import com.arangodb.entity.InvertedIndexEntity
import com.arangodb.model.CollectionCreateOptions
import one.estrondo.farango.EffectOps.flatMap
import one.estrondo.farango.EffectOps.map
import one.estrondo.farango.collection.PartiallyAppliedDeleteDocument
import one.estrondo.farango.collection.PartiallyAppliedGetDocument
import one.estrondo.farango.collection.PartiallyAppliedInsertDocument
import one.estrondo.farango.collection.PartiallyAppliedUpdateDocument

trait Collection:

  def arango: ArangoCollection

  def database: Database

  def name: String

  // noinspection MutatorLikeMethodIsParameterless
  def deleteDocument[S]: PartiallyAppliedDeleteDocument[S] = PartiallyAppliedDeleteDocument(arango)

  /** @tparam S
    *   The Stored Document's type.
    */
  def getDocument[S]: PartiallyAppliedGetDocument[S] = PartiallyAppliedGetDocument(arango)

  // noinspection MutatorLikeMethodIsParameterless
  /** @tparam S The Stored Document's type. */
  def insertDocument[S]: PartiallyAppliedInsertDocument[S] = PartiallyAppliedInsertDocument(arango)

  /** @tparam S
    *   The Stored Document's type.
    * @tparam U
    *   The type which is used to update the document in collection.
    */
  def updateDocument[S, U]: PartiallyAppliedUpdateDocument[S, U] = PartiallyAppliedUpdateDocument(arango)

object Collection:

  def apply[F[_]: Effect](
      database: Database,
      name: String,
      options: Option[CollectionCreateOptions] = None
  )(indexes: IndexEnsurer*): F[Collection] =
    Impl(database, name, options, indexes).create()
  private class Impl(
      val database: Database,
      val name: String,
      options: Option[CollectionCreateOptions],
      indexes: Seq[IndexEnsurer]
  ) extends Collection:

    val arango = database.arango.collection(name)

    def create[F[_]: Effect](): F[Collection] =
      for
        _ <- Effect[F].attemptBlocking {
               options match
                 case Some(options) => arango.create(options)
                 case None          => arango.create()
             }
        _ <- createIndexes()
      yield this

    private def createIndexes[F[_]: Effect](): F[Seq[IndexEntity | InvertedIndexEntity]] =
      for result <- Effect[F].foreach(indexes) { index =>
                      index(arango)
                    }
      yield result

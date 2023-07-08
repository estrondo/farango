package one.estrondo.farango

import one.estrondo.farango.collection.PartialDeleteDocument
import one.estrondo.farango.collection.PartialGetDocument
import one.estrondo.farango.collection.PartialInsertDocument
import one.estrondo.farango.collection.PartialUpdateDocument

/** It represents a Document Collection. */
trait Collection extends Composed:

  type CollectionRep <: Collection

  def name: String

  def database: Database

  def exists[F[_]: Effect]: F[Boolean] =
    blockingCompose(_exists)

  /** It creates the collection on the database server. */
  def create[F[_]: Effect](): F[CollectionRep] =
    blockingCompose(_create())

  /** It returns a document by key.
    * @tparam A
    *   Stored Document Representation.
    * @tparam R
    *   Return type.
    */
  def getDocument[A, R]: PartialGetDocument[A, R]

  /** It inserts a document in the collection.
    * @tparam A
    *   Stored Document Representation.
    * @tparam R
    *   Return type.
    */
  // noinspection ScalaUnusedSymbol,MutatorLikeMethodIsParameterless
  def insertDocument[A, R]: PartialInsertDocument[A, R]

  /** It deletes a document by key in the collection.
    *
    * @tparam A
    *   Stored Document Representation.
    * @tparam R
    *   Return type.
    */
  // noinspection MutatorLikeMethodIsParameterless
  def deleteDocument[A, R]: PartialDeleteDocument[A, R]

  /** It partially updates a document in the collection by key.
    *
    * @tparam A
    *   Stored Document Representation.
    * @tparam U
    *   Update Document which will be used to update.
    * @tparam R
    *   Return type.
    */
  // noinspection MutatorLikeMethodIsParameterless
  def updateDocument[A, U, R]: PartialUpdateDocument[A, U, R]

  protected def _create(): G[CollectionRep]

  protected def _exists: G[Boolean]

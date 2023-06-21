package one.estrondo.farango

import one.estrondo.farango.collection.PartialDeleteDocument
import one.estrondo.farango.collection.PartialGetDocument
import one.estrondo.farango.collection.PartialInsertDocument
import one.estrondo.farango.collection.PartialUpdateDocument

trait Collection extends Composed:

  type CollectionRep <: Collection

  def database: Database

  def create[F[_]: Effect](): F[CollectionRep] =
    blockingCompose(_create())

  def getDocument[A, R]: PartialGetDocument[A, R]

  // noinspection ScalaUnusedSymbol,MutatorLikeMethodIsParameterless
  def insertDocument[A, R]: PartialInsertDocument[A, R]

  def deleteDocument[A, R]: PartialDeleteDocument[A, R]

  def updateDocument[A, U, R]: PartialUpdateDocument[A, U, R]

  protected def _create(): G[CollectionRep]

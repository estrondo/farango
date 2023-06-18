package one.estrondo.farango

import one.estrondo.farango.collection.PartialGetDocument
import one.estrondo.farango.collection.PartialInsertDocument

trait Collection extends Composed:

  type CollectionRep <: Collection

  def database: Database

  def create[F[_]: Effect](): F[CollectionRep] =
    blockingCompose(_create())

  def getDocument[A]: PartialGetDocument[A]

  // noinspection ScalaUnusedSymbol,MutatorLikeMethodIsParameterless
  def insertDocument[A]: PartialInsertDocument[A]

  protected def _create(): G[CollectionRep]

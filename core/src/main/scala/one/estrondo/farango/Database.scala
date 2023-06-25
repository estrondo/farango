package one.estrondo.farango

import com.arangodb.model.CollectionCreateOptions
import one.estrondo.farango.database.PartialQuery

/** It represents a database. */
trait Database extends Composed:

  type DatabaseRep <: Database

  type CollectionRep <: Collection

  /** It creates the database on the database server. */
  def create[F[_]: Effect](): F[DatabaseRep] =
    compose(_create())

  /** It creates a document collection instance. */
  def collection(
      name: String,
      indexes: Seq[IndexDescription] = Nil,
      options: CollectionCreateOptions = CollectionCreateOptions()
  ): CollectionRep

  /** It executes a query. */
  def query[A, R]: PartialQuery[A, R]

  protected def _create(): G[DatabaseRep]

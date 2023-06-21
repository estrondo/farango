package one.estrondo.farango

import com.arangodb.model.CollectionCreateOptions
import one.estrondo.farango.database.PartialQuery

trait Database extends Composed:

  type DatabaseRep <: Database

  type CollectionRep <: Collection

  def create[F[_]: Effect](): F[DatabaseRep] =
    compose(_create())

  def collection(
      name: String,
      indexes: Seq[IndexDescription] = Nil,
      options: CollectionCreateOptions = CollectionCreateOptions()
  ): CollectionRep

  def query[A, R]: PartialQuery[A, R]

  protected def _create(): G[DatabaseRep]

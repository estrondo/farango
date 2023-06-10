package one.estrondo.farango

import com.arangodb.ArangoDB
import com.arangodb.model.DBCreateOptions

trait DB:

  def arango: ArangoDB
  def db[F[_]: Effect](nameOrOptions: String | DBCreateOptions, create: Boolean = false): F[Database]

object DB:

  def apply(arangoDB: ArangoDB): DB =
    new Impl(arangoDB)

  private class Impl(val arango: ArangoDB) extends DB:

    override def db[F[_]: Effect](nameOrOptions: String | DBCreateOptions, create: Boolean = false): F[Database] =
      Database(this, nameOrOptions, create)

package one.estrondo.farango

import com.arangodb.ArangoDatabase
import com.arangodb.ArangoDBException
import com.arangodb.model.CollectionCreateOptions
import com.arangodb.model.DBCreateOptions
import one.estrondo.farango.database.PartiallyAppliedQuery

trait Database:

  def arango: ArangoDatabase

  def collection[F[_]: Effect](
      name: String,
      options: Option[CollectionCreateOptions] = None,
      indexes: Seq[IndexEnsurer] = Nil
  ): F[Collection]

  def create[F[_]: Effect](): F[Database]

  def name: String

  def query[S]: PartiallyAppliedQuery[S] = PartiallyAppliedQuery(arango)

object Database:

  def apply[F[_]: Effect](db: DB, nameOrOptions: String | DBCreateOptions, create: Boolean = false): F[Database] =
    val impl = Impl(db, nameOrOptions)
    if create then impl.create() else Effect[F].succeed(impl)

  private class Impl(db: DB, nameOrOptions: DBCreateOptions | String) extends Database:

    val arango = nameOrOptions match
      case name: String             => db.arango.db(name)
      case options: DBCreateOptions => db.arango.db(options.getName)

    override def collection[F[_]: Effect](
        name: String,
        options: Option[CollectionCreateOptions] = None,
        indexes: Seq[IndexEnsurer] = Nil
    ): F[Collection] = Collection(this, name, options)(indexes*)

    override def create[F[_]: Effect](): F[Database] = Effect[F].attemptBlocking {
      if arango.exists() then this
      else
        val created = nameOrOptions match
          case name: String             => db.arango.createDatabase(name)
          case options: DBCreateOptions => db.arango.createDatabase(options)

        if created then this else throw ArangoDBException(s"Database $name was not created!")
    }

    override def name: String = nameOrOptions match
      case name: String             => name
      case options: DBCreateOptions => options.getName

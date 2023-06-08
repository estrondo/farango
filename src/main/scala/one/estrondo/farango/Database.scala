package one.estrondo.farango

import com.arangodb.ArangoDBException
import com.arangodb.model.DBCreateOptions

trait Database:

  def create[F[_]: Effect](): F[Database]

  def name: String

object Database:

  def apply[F[_]: Effect](db: DB, nameOrOptions: DBCreateOptions | String, create: Boolean = false): F[Database] =
    val impl = Impl(db, nameOrOptions)
    if create then impl.create() else Effect[F].succeed(impl)

  private class Impl(db: DB, nameOrOptions: DBCreateOptions | String) extends Database:

    private val database = nameOrOptions match
      case name: String             => db.arango.db(name)
      case options: DBCreateOptions => db.arango.db(options.getName)

    override def create[F[_]: Effect](): F[Database] = Effect[F].attempt {
      if database.exists() then this
      else
        val created = nameOrOptions match
          case _: String                => database.create()
          case options: DBCreateOptions => db.arango.createDatabase(options)

        if created then this else throw ArangoDBException(s"Database $name was not created!")
    }

    override def name: String = nameOrOptions match
      case name: String             => name
      case options: DBCreateOptions => options.getName

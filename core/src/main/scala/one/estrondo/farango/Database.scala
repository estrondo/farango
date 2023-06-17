package one.estrondo.farango

import com.arangodb.ArangoDatabase
import com.arangodb.ArangoDBException
import com.arangodb.model.CollectionCreateOptions
import com.arangodb.model.DBCreateOptions
import java.util
import one.estrondo.farango.database.PartiallyAppliedQuery
import scala.util.Try

trait Database:

  def arango: ArangoDatabase

  def root: Try[ArangoDatabase]

  def collection(
      name: String,
      indexes: Seq[IndexEnsurer] = Nil,
      options: CollectionCreateOptions = CollectionCreateOptions()
  ): Collection

  def create[F[_]: Effect](): F[Database] =
    Effect[F].attemptBlockingTry(tryCreate())

  def name: String

  def query[S]: PartiallyAppliedQuery[S] = PartiallyAppliedQuery(arango)

  def tryCreate(): Try[Database]

object Database:

  def apply(db: DB, nameOrOptions: String | DBCreateOptions): Database =
    Impl(db, nameOrOptions)

  private class Impl(db: DB, nameOrOptions: DBCreateOptions | String) extends Database:

    val arango: ArangoDatabase = nameOrOptions match
      case name: String             => db.arango.db(name)
      case options: DBCreateOptions => db.arango.db(options.getName)

    override def collection(
        name: String,
        indexes: Seq[IndexEnsurer] = Nil,
        options: CollectionCreateOptions = CollectionCreateOptions()
    ): Collection = Collection(this, name, indexes, options)

    override def tryCreate(): Try[Database] =
      for root <- db.root
      yield
        if root.db(name).exists() then this
        else
          val created = nameOrOptions match
            case _: String                => root.createDatabase(addUser(DBCreateOptions().name(name)))
            case options: DBCreateOptions => root.createDatabase(addUser(options))

          if created then this else throw ArangoDBException(s"Farango: Database $name was not created!")

    private def addUser(options: DBCreateOptions): DBCreateOptions =
      val copy  = Copy(options)
      var users = copy.getUsers
      if users == null then
        users = new util.ArrayList()
        copy.users(users)

      users.add(db.databaseUsersOptions)
      copy

    override def root: Try[ArangoDatabase] =
      for root <- db.root yield root.db(name)

    override def name: String = nameOrOptions match
      case name: String             => name
      case options: DBCreateOptions => options.getName

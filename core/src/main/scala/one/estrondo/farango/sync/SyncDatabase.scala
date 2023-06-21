package one.estrondo.farango.sync

import com.arangodb.ArangoDatabase
import com.arangodb.model.CollectionCreateOptions
import com.arangodb.model.DatabaseUsersOptions
import com.arangodb.model.DBCreateOptions
import java.util
import java.util.Collections
import one.estrondo.farango.Database
import one.estrondo.farango.IndexDescription
import one.estrondo.farango.database.PartialQuery
import one.estrondo.farango.entity.Copy
import one.estrondo.farango.sync.database.SyncPartialQuery
import scala.util.Try

trait SyncDatabase extends Database, SyncComposed:

  override type DatabaseRep = SyncDatabase

  override type CollectionRep = SyncCollection

  def arango: ArangoDatabase

  def name: String

  def root: Try[ArangoDatabase]

object SyncDatabase:

  def apply(db: SyncDB, nameOrOptions: String | DBCreateOptions): SyncDatabase =
    Impl(db, nameOrOptions)

  private class Impl(db: SyncDB, nameOrOptions: String | DBCreateOptions) extends SyncDatabase:

    val arango: ArangoDatabase = db.arango.db(name)

    override def collection(
        name: String,
        indexes: Seq[IndexDescription],
        options: CollectionCreateOptions
    ): SyncCollection = SyncCollection(this, name, indexes, options)

    override def query[S, R]: PartialQuery[S, R] = SyncPartialQuery(arango)

    override def root: Try[ArangoDatabase] =
      for root <- db.root yield root.db(name)

    override protected def _create(): Try[SyncDatabase] =
      for root <- db.root yield
        val options = nameOrOptions match
          case _: String          => DBCreateOptions().name(name)
          case o: DBCreateOptions => Copy(o)

        val user = DatabaseUsersOptions()
          .username(db.config.user.get)
          .passwd(db.config.password.get)

        if options.getUsers == null then options.users(Collections.singletonList(user))
        else
          val users = util.ArrayList(options.getUsers)
          users.add(user)
          options.users(users)

        root.createDatabase(options)
        this

    override def name: String = nameOrOptions match
      case s: String          => s
      case o: DBCreateOptions => o.getName

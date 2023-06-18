package one.estrondo.farango.sync

import com.arangodb.ArangoDB
import com.arangodb.entity.UserEntity
import com.arangodb.model.DBCreateOptions
import com.arangodb.model.UserCreateOptions
import one.estrondo.farango.Config
import one.estrondo.farango.ConfigBuilder
import one.estrondo.farango.DB
import scala.util.Try

trait SyncDB extends DB, SyncComposed:

  override type DatabaseRep = SyncDatabase
  def arango: ArangoDB

  def root: Try[ArangoDB]

object SyncDB:

  def apply(config: Config)(using ConfigBuilder[ArangoDB]): Try[SyncDB] =
    for arango <- summon[ConfigBuilder[ArangoDB]].user(config)
    yield Impl(arango, config)

  private[farango] class Impl(val arango: ArangoDB, val config: Config)(using ConfigBuilder[ArangoDB]) extends SyncDB:

    override def database(nameOrOptions: String | DBCreateOptions): SyncDatabase =
      SyncDatabase(this, nameOrOptions)

    override protected def _createUser(user: String, password: String, options: UserCreateOptions): Try[UserEntity] =
      for root <- this.root yield root.createUser(user, password, options)

    override def root: Try[ArangoDB] =
      summon[ConfigBuilder[ArangoDB]].root(config)

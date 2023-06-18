package one.estrondo.farango

import com.arangodb.entity.UserEntity
import com.arangodb.model.DBCreateOptions
import com.arangodb.model.UserCreateOptions
import one.estrondo.farango.sync.SyncDB
import scala.util.Try

trait DB extends Composed:

  type DatabaseRep <: Database

  def config: Config

  def database(nameOrOptions: String | DBCreateOptions): DatabaseRep

  def createUser[F[_]: Effect](
      user: String,
      password: String,
      options: UserCreateOptions = UserCreateOptions()
  ): F[UserEntity] = blockingCompose(_createUser(user, password, options))

  protected def _createUser(user: String, password: String, options: UserCreateOptions): G[UserEntity]

object DB:

  def fromSync[F[+_]: Effect](builder: Config): F[SyncDB] =
    Effect[F].fromTry(sync(builder))

  def sync(builder: Config): Try[SyncDB] = ???

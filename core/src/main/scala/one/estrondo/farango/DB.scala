package one.estrondo.farango

import com.arangodb.ArangoDB
import com.arangodb.ArangoDBException
import com.arangodb.entity.UserEntity
import com.arangodb.model.DatabaseUsersOptions
import com.arangodb.model.DBCreateOptions
import com.arangodb.model.UserCreateOptions
import scala.util.Failure
import scala.util.Try

trait DB:

  def arango: ArangoDB

  def root: Try[ArangoDB]

  def createUser[F[_]: Effect](
      user: String,
      password: String,
      options: UserCreateOptions = UserCreateOptions()
  ): F[UserEntity] = Effect[F].attemptBlockingTry(tryCreateUser(user, password, options))

  def databaseUsersOptions: DatabaseUsersOptions

  def db(nameOrOptions: String | DBCreateOptions): Database

  def tryCreateUser(user: String, password: String, options: UserCreateOptions = UserCreateOptions()): Try[UserEntity]

object DB:

  def apply[F[_]: Effect](builder: ArangoBuilder): F[DB] =
    Effect[F].attemptBlockingTry(tryApply(builder))

  def tryApply(builder: ArangoBuilder): Try[DB] =
    for arango <- builder.build() yield new Impl(arango, builder)

  private class Impl(val arango: ArangoDB, protected val builder: ArangoBuilder) extends DB:

    override def tryCreateUser(
        user: String,
        password: String,
        options: UserCreateOptions = UserCreateOptions()
    ): Try[UserEntity] =
      for root <- this.root yield root.createUser(user, password, options)

    def root: Try[ArangoDB] =
      builder.rootPassword match
        case Some(password) =>
          builder
            .copy(
              user = Some("root"),
              password = Some(password),
              maxConnections = Some(1),
              serde = None
            )
            .build()

        case None => Failure(ArangoDBException("Farango: You need to configure the root's password!"))

    override def databaseUsersOptions: DatabaseUsersOptions =
      DatabaseUsersOptions()
        .username(builder.user.get)
        .passwd(builder.password.get)
        .active(true)

    override def db(nameOrOptions: String | DBCreateOptions): Database =
      Database(this, nameOrOptions)

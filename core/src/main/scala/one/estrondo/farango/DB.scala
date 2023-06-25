package one.estrondo.farango

import com.arangodb.ArangoDBException
import com.arangodb.entity.UserEntity
import com.arangodb.model.DBCreateOptions
import com.arangodb.model.UserCreateOptions
import one.estrondo.farango.EffectOps.flatMap
import one.estrondo.farango.EffectOps.map
import scala.util.Failure
import scala.util.Success

/** It represents a Arango Database Server. */
trait DB extends Composed:

  type DatabaseRep <: Database

  /** Farango Configuration. */
  def config: Config

  /** It returns an instance of Farango Database. */
  def database(nameOrOptions: String | DBCreateOptions): DatabaseRep

  /** It creates a user using the information provided by Farango Configuration. */
  def createDefaultUser[F[_]: Effect](options: UserCreateOptions = UserCreateOptions()): F[UserEntity] =
    for
      userAndPassword <-
        Effect[F].fromTry(
          (config.user, config.password) match
            case (Some(user), Some(password)) =>
              Success(user, password)

            case _ =>
              Failure(ArangoDBException("Farango: You have to define both a default username and password."))
        )

      (user, password) = userAndPassword
      entity          <- createUser(user, password, options)
    yield entity

  /** It creates a user. */
  def createUser[F[_]: Effect](
      user: String,
      password: String,
      options: UserCreateOptions = UserCreateOptions()
  ): F[UserEntity] = blockingCompose(_createUser(user, password, options))

  protected def _createUser(user: String, password: String, options: UserCreateOptions): G[UserEntity]

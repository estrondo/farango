package one.estrondo.farango

import com.arangodb.ArangoDB
import com.arangodb.entity.UserEntity
import com.arangodb.model.UserCreateOptions
import one.estrondo.farango.sync.SyncDB
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.language.implicitConversions
import scala.util.Success
import scala.util.Try

abstract class DBSpec[F[_]: Effect: EffectToFuture, S[_]](using EffectStream[S, F], EffectStreamCollector[S, F])
    extends FarangoSpec[F, S]:

  protected val config: Config = Config()
    .withUser("test-user")
    .withPassword("test-password")
    .withRootPassword("root-password")

  "A DB instance:" - {
    "It should create a user." in {
      for
        db     <- createUser
        entity <- db.createUser("test-user", "test-password")
      yield entity shouldNot be(null)
    }
  }

  protected def createUser: F[DB]

abstract class SyncDBSpec[F[_]: Effect: EffectToFuture, S[_]](using EffectStream[S, F], EffectStreamCollector[S, F])
    extends DBSpec[F, S]:

  override protected def createUser: F[DB] =
    val (user, root, builder) = createArango()

    when(root.createUser(eqTo(config.user.get), eqTo(config.password.get), any[UserCreateOptions]))
      .thenReturn(UserEntity())

    Effect[F].fromTry(Try {
      SyncDB.Impl(user, config)(using builder)
    })

  private def createArango(): (ArangoDB, ArangoDB, ConfigBuilder[ArangoDB]) =
    val userArango = mock[ArangoDB]
    val rootArango = mock[ArangoDB]
    val builder    = mock[ConfigBuilder[ArangoDB]]

    when(builder.user(config)).thenReturn(Success(userArango))
    when(builder.root(config)).thenReturn(Success(rootArango))
    (userArango, rootArango, builder)

class SyncDBSpecWithTry extends SyncDBSpec[Try, Vector]

class SyncDBSpecWithEither extends SyncDBSpec[[X] =>> Either[Throwable, X], Vector]

class SyncDBSpecWithFuture extends SyncDBSpec[Future, Vector]

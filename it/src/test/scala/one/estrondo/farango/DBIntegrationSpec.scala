package one.estrondo.farango

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.Try

abstract class DBIntegrationSpec[F[+_]: Effect: EffectToFuture, S[_]](using
    EffectStream[S, F],
    EffectStreamCollector[S, F]
) extends FarangoIntegrationSpec[F, S]:

  "A DB instance" - {
    "It should create a user." in withDB(createUser = false) { db =>
      for entity <- db.createUser("einstein", "emc2")
      yield entity shouldNot be(null)
    }

    "It should create the default user." in withDB(createUser = false) { db =>
      for entity <- db.createDefaultUser()
      yield entity.getUser should be(db.config.user)
    }
  }

abstract class SyncDBIntegrationSpec[F[+_]: Effect: EffectToFuture, S[_]](using
    EffectStream[S, F],
    EffectStreamCollector[S, F]
) extends DBIntegrationSpec[F, S],
      TestSyncDB

class SyncDBIntegrationSpecWithTry extends SyncDBIntegrationSpec[Try, Vector]

class SyncDBIntegrationSpecWithEither extends SyncDBIntegrationSpec[[X] =>> Either[Throwable, X], Vector]

class SyncDBIntegrationSpecWithFuture extends SyncDBIntegrationSpec[Future, Vector]

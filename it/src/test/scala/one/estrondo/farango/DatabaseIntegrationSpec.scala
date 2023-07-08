package one.estrondo.farango

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.Try

abstract class DatabaseIntegrationSpec[F[+_]: Effect: EffectToFuture, S[_]](using
    EffectStream[S, F],
    EffectStreamCollector[S, F]
) extends FarangoIntegrationSpec[F, S]:

  "A Database instance" - {

    "It should provides the method .exists, when the database doesn't exist." in withDatabase(create = false) {
      database =>
        for exists <- database.exists yield exists should be(false)
    }

    "It should provides the method .exists, when the database exists." in withDatabase() { database =>
      for exists <- database.exists yield exists should be(true)
    }
  }

abstract class SyncDatabaseIntegrationSpec[F[+_]: Effect: EffectToFuture, S[_]](using
    EffectStream[S, F],
    EffectStreamCollector[S, F]
) extends DatabaseIntegrationSpec[F, S],
      TestSyncDB

class SyncDatabaseIntegrationSpecWithTry extends SyncDatabaseIntegrationSpec[Try, Vector]

class SyncDatabaseIntegrationSpecWithEither extends SyncDatabaseIntegrationSpec[[X] =>> Either[Throwable, X], List]

class SyncDatabaseIntegrationSpecWithFuture extends SyncDatabaseIntegrationSpec[Future, Set]

package one.estrondo.farango

import com.arangodb.model.DatabaseOptions
import com.arangodb.model.DBCreateOptions
import one.estrondo.farango.EffectOps.map
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.language.implicitConversions
import scala.util.Try

abstract class DBIntegrationSpec[F[_]: Effect: EffectToFuture, S[_]](using StreamEffectToEffect[S])
    extends FarangoIntegrationSpec[F, S]:

  "A DB" - {
    "It should create a new database with a name." in withDB { db =>
      for database <- DB(db).db("test-database").create()
      yield database.arango.name() should be("test-database")
    }

    "It should create a new database with options." in withDB { db =>
      for database <-
          DB(db)
            .db(DBCreateOptions().name("test-database").options(DatabaseOptions().sharding("test-sharding")))
            .create()
      yield database.arango.exists() should be(true)
    }
  }

class DBIntegrationSpecWithTry extends DBIntegrationSpec[Try, Vector]

class DBIntegrationSpecWithEither extends DBIntegrationSpec[[X] =>> Either[Throwable, X], Vector]

class DBIntegrationSpecWithFuture extends DBIntegrationSpec[[X] =>> Future[X], Vector]

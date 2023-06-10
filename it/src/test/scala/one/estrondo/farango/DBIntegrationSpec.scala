package one.estrondo.farango

import com.arangodb.model.DatabaseOptions
import com.arangodb.model.DBCreateOptions
import scala.util.Try

abstract class DBIntegrationSpec[F[_]: Effect: EffectToFuture] extends FarangoIntegrationSpec[F]:

  "A DB" - {
    "It should create a new database with a name." in withDB { db =>
      for database <- DB(db).db("test-database", true)
      yield database.arango.name() should be("test-database")
    }

    "It should create a new database with options." in withDB { db =>
      for database <-
          DB(db).db(DBCreateOptions().name("test-database").options(DatabaseOptions().sharding("test-sharding")), true)
      yield database.arango.exists() should be(true)
    }
  }

class DBIntegrationSpecWithTry    extends DBIntegrationSpec[Try]
class DBIntegrationSpecWithEither extends DBIntegrationSpec[[X] =>> Either[Throwable, X]]

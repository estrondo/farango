package one.estrondo.farango

import com.arangodb.ArangoDatabase
import com.arangodb.ArangoDB
import com.arangodb.model.DBCreateOptions
import scala.util.Try

abstract class DBSpec[F[_]: Effect: EffectToFuture] extends FarangoSpec[F]:

  "A DB" - {
    "It should create a database by name." in {
      val arangoDB               = mock[ArangoDB]
      val expectedArangoDatabase = mock[ArangoDatabase]

      when(arangoDB.db("test-database"))
        .thenReturn(expectedArangoDatabase)

      when(arangoDB.createDatabase("test-database"))
        .thenReturn(true)

      for database <- DB(arangoDB).db("test-database", true)
      yield database.arango should be(expectedArangoDatabase)
    }

    "It should create a database by options." in {
      val arangoDB               = mock[ArangoDB]
      val expectedArangoDatabase = mock[ArangoDatabase]
      val options                = DBCreateOptions().name("test-database")

      when(arangoDB.db("test-database"))
        .thenReturn(expectedArangoDatabase)

      when(arangoDB.createDatabase(options))
        .thenReturn(true)

      for database <- DB(arangoDB).db(options, true)
      yield database.arango should be(expectedArangoDatabase)
    }
  }

class DBSpecWithTry extends DBSpec[Try]

class DBSpecWithEither extends DBSpec[[X] =>> Either[Throwable, X]]

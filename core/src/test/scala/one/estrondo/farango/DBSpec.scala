package one.estrondo.farango

import com.arangodb.ArangoDatabase
import com.arangodb.ArangoDB
import com.arangodb.model.DBCreateOptions
import one.estrondo.farango.EffectOps.map
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.language.implicitConversions
import scala.util.Try

abstract class DBSpec[F[_]: Effect: EffectToFuture, S[_]](using StreamEffectToEffect[S]) extends FarangoSpec[F, S]:

  "A DB" - {
    "It should create a database by name." in {
      val arangoDB               = mock[ArangoDB]
      val expectedArangoDatabase = mock[ArangoDatabase]

      when(arangoDB.db("test-database"))
        .thenReturn(expectedArangoDatabase)

      when(arangoDB.createDatabase("test-database"))
        .thenReturn(true)

      val database = DB(arangoDB).db("test-database")

      for _ <- database.create()
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

      val database = DB(arangoDB).db(options)

      for _ <- database.create()
      yield database.arango should be(expectedArangoDatabase)
    }
  }

class DBSpecWithTry extends DBSpec[Try, Vector]

class DBSpecWithEither extends DBSpec[[X] =>> Either[Throwable, X], Vector]

class DBSpecWithFuture extends DBSpec[[X] =>> Future[X], Vector]

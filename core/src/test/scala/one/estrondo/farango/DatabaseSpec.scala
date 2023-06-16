package one.estrondo.farango

import com.arangodb.ArangoCursor
import com.arangodb.ArangoDatabase
import com.arangodb.ArangoDB
import com.arangodb.model.AqlQueryOptions
import java.util.stream
import one.estrondo.farango.EffectOps.flatMap
import one.estrondo.farango.EffectOps.map
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.language.implicitConversions
import scala.util.Try

abstract class DatabaseSpec[F[_]: Effect: EffectToFuture, S[_]](using StreamEffect[S, F], StreamEffectToEffect[S])
    extends FarangoSpec[F, S]:

  protected def createMockContext(): MockContext =
    val db             = mock[DB]
    val arangoDB       = mock[ArangoDB]
    val arangoDatabase = mock[ArangoDatabase]

    when(db.arango)
      .thenReturn(arangoDB)

    when(arangoDB.db("test-database"))
      .thenReturn(arangoDatabase)

    MockContext(
      arangoDB = arangoDB,
      arangoDatabase = arangoDatabase,
      db = db,
      database = Database(db, "test-database")
    )

  "A Database" - {
    "It should execute a simple query in database." in {
      val ctx = createMockContext()
      import ctx.*

      val cursor        = mock[ArangoCursor[StoredDocument]]
      val expectedQuery = "FOR document IN collection RETURN document"

      when(
        arangoDatabase.query(
          eqTo(expectedQuery),
          eqTo(classOf[StoredDocument]),
          any[java.util.Map[String, AnyRef]],
          any[AqlQueryOptions]
        )
      )
        .thenReturn(cursor)

      when(cursor.stream())
        .thenReturn(stream.Stream.of(StoredDocument(_key = "A", name = "B")))

      for documents <- database.query[StoredDocument](expectedQuery).collect()
      yield documents should contain only (UserDocument(_key = "A", name = "B"))
    }
  }

  case class StoredDocument(_key: String, name: String)
  case class UserDocument(_key: String, name: String)

  protected case class MockContext(
      arangoDB: ArangoDB,
      arangoDatabase: ArangoDatabase,
      db: DB,
      database: Database
  )

  given Transformer[StoredDocument, UserDocument] with
    override def apply[F[_]: Effect](a: StoredDocument): F[UserDocument] =
      Effect[F].attempt {
        UserDocument(a._key, a.name)
      }

class DatabaseSpecWithTry extends DatabaseSpec[Try, Vector]

class DatabaseSpecWithEither extends DatabaseSpec[[X] =>> Either[Throwable, X], Vector]

class DatabaseSpecWithFuture extends DatabaseSpec[[X] =>> Future[X], Vector]

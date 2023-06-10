package one.estrondo.farango

import com.arangodb.ArangoCollection
import com.arangodb.ArangoDatabase
import com.arangodb.entity.CollectionEntity
import com.arangodb.entity.IndexEntity
import com.arangodb.model.CollectionCreateOptions
import com.arangodb.model.DocumentReadOptions
import org.mockito.Mockito
import org.scalatest.matchers.HavePropertyMatcher
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.language.implicitConversions
import scala.util.Try

abstract class CollectionSpec[F[_]: Effect: EffectToFuture] extends FarangoSpec[F]:

  protected def createMockContext(): MockContext =
    val database         = mock[Database]
    val arangoDatabase   = mock[ArangoDatabase]
    val arangoCollection = mock[ArangoCollection]

    when(database.arango).thenReturn(arangoDatabase)
    when(arangoDatabase.collection("test-collection"))
      .thenReturn(arangoCollection)

    when(arangoCollection.create())
      .thenReturn(CollectionEntity())
    when(arangoCollection.create(any[CollectionCreateOptions]))
      .thenReturn(CollectionEntity())

    MockContext(database, arangoDatabase, arangoCollection)

  case class StoredDocument(_key: String, name: String)

  case class UserDocument(_key: String, name: String)

  protected class MockContext(
      val database: Database,
      val arangoDatabase: ArangoDatabase,
      val arangoCollection: ArangoCollection
  )

  given Transformer[StoredDocument, UserDocument] with

    override def apply[F[_]: Effect](a: StoredDocument): F[UserDocument] =
      Effect[F].succeed(UserDocument(a._key, a.name))

  "A Collection" - {

    "It should create a Arango's collection without options." in {
      val arango         = mock[ArangoDatabase]
      val database       = mock[Database]
      val mockCollection = mock[ArangoCollection]

      when(database.arango)
        .thenReturn(arango)

      when(arango.collection("test-collection"))
        .thenReturn(mockCollection)

      when(mockCollection.create())
        .thenReturn(CollectionEntity())

      for collection <- Collection(database, "test-collection")()
      yield collection should have(
        Symbol("name")("test-collection"),
        Symbol("arango")(mockCollection)
      )
    }

    "It should create a Arango's collection with options." in {
      val arango         = mock[ArangoDatabase]
      val database       = mock[Database]
      val mockCollection = mock[ArangoCollection]
      val options        = CollectionCreateOptions().numberOfShards(10)

      when(database.arango)
        .thenReturn(arango)

      when(arango.collection("test-collection"))
        .thenReturn(mockCollection)

      when(mockCollection.create(options))
        .thenReturn(CollectionEntity())

      for collection <- Collection(database, "test-collection", Some(options))()
      yield collection should have(
        Symbol("name")("test-collection"),
        Symbol("arango")(mockCollection)
      )
    }

    "It should a Arango's collection with indexes" in {
      val arango         = mock[ArangoDatabase]
      val database       = mock[Database]
      val mockCollection = mock[ArangoCollection]
      val indexEnsurer   = mock[IndexEnsurer]

      when(database.arango)
        .thenReturn(arango)

      when(arango.collection("test-collection"))
        .thenReturn(mockCollection)

      when(mockCollection.create())
        .thenReturn(CollectionEntity())

      when(indexEnsurer(mockCollection))
        .thenReturn(Effect[F].succeed(IndexEntity()))

      for collection <- Collection(database, "test-collection")(indexEnsurer)
      yield collection should have(
        Symbol("name")("test-collection"),
        Symbol("arango")(mockCollection)
      )
    }

    "It should get a document by key." in {
      val mockContext = createMockContext()
      import mockContext.*

      when(arangoCollection.getDocument(eqTo("key"), eqTo(classOf[StoredDocument]), any[DocumentReadOptions]))
        .thenReturn(StoredDocument("42", "Adams"))

      val expectedUserDocument = UserDocument("42", "Adams")

      for
        collection <- Collection(database, "test-collection")()
        result     <- collection.getDocument[StoredDocument].apply("key")
      yield result should contain(expectedUserDocument)
    }
  }

class CollectionSpecWithTry extends CollectionSpec[Try]

class CollectionSpecWithEither extends CollectionSpec[[X] =>> Either[Throwable, X]]

class CollectionSpecWithFuture extends CollectionSpec[[X] =>> Future[X]]

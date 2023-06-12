package one.estrondo.farango

import com.arangodb.ArangoCollection
import com.arangodb.ArangoDatabase
import com.arangodb.entity.CollectionEntity
import com.arangodb.entity.DocumentCreateEntity
import com.arangodb.entity.DocumentDeleteEntity
import com.arangodb.entity.DocumentUpdateEntity
import com.arangodb.entity.IndexEntity
import com.arangodb.model.CollectionCreateOptions
import com.arangodb.model.DocumentCreateOptions
import com.arangodb.model.DocumentDeleteOptions
import com.arangodb.model.DocumentReadOptions
import com.arangodb.model.DocumentUpdateOptions
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

    val collection = Collection(database, "test-collection")()

    MockContext(database, arangoDatabase, arangoCollection, collection)

  case class StoredDocument(_key: String, name: String)

  case class UserDocument(_key: String, name: String)

  case class UpdateDocument(name: String, email: String)

  protected class MockContext(
      val database: Database,
      val arangoDatabase: ArangoDatabase,
      val arangoCollection: ArangoCollection,
      val getCollection: F[Collection]
  )

  given Transformer[StoredDocument, UserDocument] with

    override def apply[F[_]: Effect](a: StoredDocument): F[UserDocument] =
      Effect[F].succeed(UserDocument(a._key, a.name))

  given Transformer[UserDocument, StoredDocument] with
    override def apply[F[_]: Effect](a: UserDocument): F[StoredDocument] =
      Effect[F].succeed(StoredDocument(a._key, a.name))

  given Transformer[UserDocument, UpdateDocument] with
    override def apply[F[_]: Effect](a: UserDocument): F[UpdateDocument] =
      Effect[F].succeed(UpdateDocument(a.name, email = null.asInstanceOf[String]))

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

    "It should insert a document." in {
      val mockContext = createMockContext()
      import mockContext.*

      val toInsert = UserDocument("42", "Galileo")
      val toStore  = StoredDocument("42", "Galileo")

      val entity = DocumentCreateEntity[StoredDocument]()
      entity.setNew(toStore)

      when(
        arangoCollection.insertDocument(
          eqTo(StoredDocument("42", "Galileo")),
          any[DocumentCreateOptions],
          eqTo(classOf[StoredDocument])
        )
      )
        .thenReturn(entity)

      for
        collection <- getCollection
        result     <- collection.insertDocument[StoredDocument](toInsert, DocumentCreateOptions().waitForSync(true))
      yield result.getNew should be(toStore)
    }

    "It should update a document." in {
      val context = createMockContext()
      import context.*

      val userInput        = UserDocument(_key = "99", name = "Ronaldo")
      val toUpdate         = UpdateDocument(name = "Ronaldo", email = null)
      val expectedDocument = StoredDocument("77", "Aka aka")
      val entity           = DocumentUpdateEntity[StoredDocument]()

      entity.setNew(expectedDocument)

      when(
        arangoCollection.updateDocument(
          eqTo("99"),
          eqTo(toUpdate),
          any[DocumentUpdateOptions],
          eqTo(classOf[StoredDocument])
        )
      )
        .thenReturn(entity)

      for
        collection <- getCollection
        entity     <- collection.updateDocument[StoredDocument, UpdateDocument]("99", userInput)
      yield entity.getNew should be(expectedDocument)
    }

    "It should remove a document." in {
      val context = createMockContext()
      import context.*

      val expectedOldDocument = StoredDocument("852", "Leila")
      val entity              = DocumentDeleteEntity[StoredDocument]()

      entity.setOld(expectedOldDocument)

      when(
        arangoCollection.deleteDocument(eqTo("852"), any[DocumentDeleteOptions], eqTo(classOf[StoredDocument]))
      ).thenReturn(entity)

      for
        collection <- getCollection
        result     <- collection.deleteDocument[StoredDocument]("852")
      yield
        val (entity, oldDocument) = result
        entity.getOld should be(expectedOldDocument)
        oldDocument should contain(UserDocument("852", "Leila"))

    }
  }

class CollectionSpecWithTry extends CollectionSpec[Try]

class CollectionSpecWithEither extends CollectionSpec[[X] =>> Either[Throwable, X]]

class CollectionSpecWithFuture extends CollectionSpec[[X] =>> Future[X]]

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
import one.estrondo.farango.EffectOps.flatMap
import one.estrondo.farango.EffectOps.map
import org.scalatest.matchers.HavePropertyMatcher
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.language.implicitConversions
import scala.util.Success
import scala.util.Try

abstract class CollectionSpec[F[_]: Effect: EffectToFuture, S[_]](using StreamEffectToEffect[S])
    extends FarangoSpec[F, S]:

  protected def createMockContext(): MockContext =
    val database             = mock[Database]
    val userArangoDatabase   = mock[ArangoDatabase]
    val userArangoCollection = mock[ArangoCollection]
    val rootArangoDatabase   = mock[ArangoDatabase]
    val rootArangoCollection = mock[ArangoCollection]

    when(database.arango).thenReturn(userArangoDatabase)
    when(database.root)
      .thenReturn(Success(rootArangoDatabase))

    when(userArangoDatabase.collection("test-collection"))
      .thenReturn(userArangoCollection)
    when(rootArangoDatabase.collection("test-collection"))
      .thenReturn(rootArangoCollection)

    val collection = Collection(database, "test-collection")

    new MockContext(
      database = database,
      userArangoDatabase = userArangoDatabase,
      userArangoCollection = userArangoCollection,
      rootArangoDatabase = rootArangoDatabase,
      rootArangoCollection = rootArangoCollection,
      collection = collection
    )

  case class StoredDocument(_key: String, name: String)

  case class UserDocument(_key: String, name: String)

  case class UpdateDocument(name: String, email: String)

  protected class MockContext(
      val database: Database,
      val userArangoDatabase: ArangoDatabase,
      val userArangoCollection: ArangoCollection,
      val rootArangoDatabase: ArangoDatabase,
      val rootArangoCollection: ArangoCollection,
      val collection: Collection
  ):

    def withCreationMock(): MockContext =
      when(rootArangoCollection.create(any[CollectionCreateOptions]))
        .thenReturn(CollectionEntity())

      this

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
      val ctx = createMockContext()
      import ctx.*

      when(rootArangoCollection.create(any[CollectionCreateOptions]))
        .thenReturn(CollectionEntity())

      for collection <- Collection(database, "test-collection").create()
      yield collection should have(
        Symbol("name")("test-collection"),
        Symbol("arango")(userArangoCollection)
      )
    }

    "It should create a Arango's collection with options." in {
      val ctx = createMockContext()
      import ctx.*

      val options = CollectionCreateOptions().numberOfShards(10)
      when(rootArangoCollection.create(eqTo(options)))
        .thenReturn(CollectionEntity())

      for collection <- Collection(database, "test-collection", Nil, options).create()
      yield collection should have(
        Symbol("name")("test-collection"),
        Symbol("arango")(userArangoCollection)
      )
    }

    "It should a Arango's collection with indexes" in {
      val ctx = createMockContext().withCreationMock()
      import ctx.*

      val indexEnsurer = mock[IndexEnsurer]

      when(indexEnsurer(rootArangoCollection))
        .thenReturn(Success(IndexEntity()))

      for collection <- Collection(database, "test-collection", Seq(indexEnsurer)).create()
      yield collection should have(
        Symbol("name")("test-collection"),
        Symbol("arango")(userArangoCollection)
      )
    }

    "It should get a document by key." in {
      val ctx = createMockContext().withCreationMock()
      import ctx.*

      when(userArangoCollection.getDocument(eqTo("key"), eqTo(classOf[StoredDocument]), any[DocumentReadOptions]))
        .thenReturn(StoredDocument("42", "Adams"))

      val expectedUserDocument = UserDocument("42", "Adams")

      for
        collection <- Collection(database, "test-collection").create()
        result     <- collection.getDocument[StoredDocument].apply("key")
      yield result should contain(expectedUserDocument)
    }

    "It should insert a document." in {
      val ctx = createMockContext().withCreationMock()
      import ctx.*

      val toInsert = UserDocument("42", "Galileo")
      val toStore  = StoredDocument("42", "Galileo")

      val entity = DocumentCreateEntity[StoredDocument]()
      entity.setNew(toStore)

      when(
        userArangoCollection.insertDocument(
          eqTo(StoredDocument("42", "Galileo")),
          any[DocumentCreateOptions],
          eqTo(classOf[StoredDocument])
        )
      )
        .thenReturn(entity)

      for
        collection <- collection.create()
        result     <- collection.insertDocument[StoredDocument](toInsert, DocumentCreateOptions().waitForSync(true))
      yield result.getNew should be(toStore)
    }

    "It should update a document." in {
      val ctx = createMockContext().withCreationMock()
      import ctx.*

      val userInput        = UserDocument(_key = "99", name = "Ronaldo")
      val toUpdate         = UpdateDocument(name = "Ronaldo", email = null)
      val expectedDocument = StoredDocument("77", "Aka aka")
      val entity           = DocumentUpdateEntity[StoredDocument]()

      entity.setNew(expectedDocument)

      when(
        userArangoCollection.updateDocument(
          eqTo("99"),
          eqTo(toUpdate),
          any[DocumentUpdateOptions],
          eqTo(classOf[StoredDocument])
        )
      )
        .thenReturn(entity)

      for
        collection <- collection.create()
        entity     <- collection.updateDocument[StoredDocument, UpdateDocument]("99", userInput)
      yield entity.getNew should be(expectedDocument)
    }

    "It should remove a document." in {
      val ctx = createMockContext().withCreationMock()
      import ctx.*

      val expectedOldDocument = StoredDocument("852", "Leila")
      val entity              = DocumentDeleteEntity[StoredDocument]()

      entity.setOld(expectedOldDocument)

      when(
        userArangoCollection.deleteDocument(eqTo("852"), any[DocumentDeleteOptions], eqTo(classOf[StoredDocument]))
      ).thenReturn(entity)

      for
        collection <- collection.create()
        entity     <- collection.deleteDocument[StoredDocument]("852")
      yield entity.getOld should be(StoredDocument("852", "Leila"))

    }
  }

class CollectionSpecWithTry extends CollectionSpec[Try, Vector]

class CollectionSpecWithEither extends CollectionSpec[[X] =>> Either[Throwable, X], Vector]

class CollectionSpecWithFuture extends CollectionSpec[[X] =>> Future[X], Vector]

package one.estrondo.farango

import com.arangodb.model.DocumentCreateOptions
import com.arangodb.model.DocumentDeleteOptions
import com.arangodb.model.DocumentUpdateOptions
import one.estrondo.farango.CollectionIntegrationSpec.StoredDocument
import one.estrondo.farango.CollectionIntegrationSpec.UpdateDocument
import one.estrondo.farango.CollectionIntegrationSpec.UserDocument
import one.estrondo.farango.CollectionIntegrationSpec.UserUpdateDocument
import one.estrondo.farango.EffectOps.flatMap
import one.estrondo.farango.EffectOps.map
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.language.implicitConversions
import scala.util.Try

abstract class CollectionIntegrationSpec[F[_]: Effect: EffectToFuture, S[_]](using StreamEffectToEffect[S])
    extends FarangoIntegrationSpec[F, S]:

  given Transformer[UserDocument, StoredDocument] with
    override def apply[F[_]: Effect](a: UserDocument): F[StoredDocument] =
      Effect[F].succeed(StoredDocument(a._key, a.name))

  given Transformer[StoredDocument, UserDocument] with
    override def apply[F[_]: Effect](a: StoredDocument): F[UserDocument] =
      Effect[F].succeed(UserDocument(a._key, a.name))

  given Transformer[UserUpdateDocument, UpdateDocument] with
    override def apply[F[_]: Effect](a: UserUpdateDocument): F[UpdateDocument] =
      Effect[F].succeed(UpdateDocument(a.name))

  "A Collection" - {

    "It should insert a document into." in withCollection { collection =>
      val toInsert = UserDocument("42", "Currie")
      val toStore  = StoredDocument("42", "Currie")
      for
        entity <-
          collection.insertDocument[StoredDocument](toInsert, DocumentCreateOptions().waitForSync(true).returnNew(true))
        stored <- collection.getDocument[StoredDocument]("42")
      yield
        entity.getNew should be(toStore)
        stored should contain(toInsert)
    }

    "It should update a document." in withCollection { collection =>
      val storedDocument = UserDocument("33", "Angela")

      for
        _       <- collection.insertDocument[StoredDocument](storedDocument, DocumentCreateOptions().waitForSync(true))
        entity  <- collection.updateDocument[StoredDocument, UpdateDocument](
                     "33",
                     UserUpdateDocument("Flor Angela"),
                     DocumentUpdateOptions().waitForSync(true).returnNew(true).returnOld(true)
                   )
        updated <- collection.getDocument[UserDocument]("33")
      yield
        entity.getNew should be(StoredDocument(_key = "33", name = "Flor Angela"))
        entity.getOld should be(StoredDocument(_key = "33", name = "Angela"))
        updated should contain(StoredDocument(_key = "33", name = "Flor Angela"))
    }

    "It should delete a document." in withCollection { collection =>
      val storedDocument = UserDocument("33", "Angela")

      for
        _        <- collection.insertDocument[StoredDocument](storedDocument, DocumentCreateOptions().waitForSync(true))
        entity   <-
          collection.deleteDocument[StoredDocument]("33", DocumentDeleteOptions().waitForSync(true).returnOld(true))
        notFound <- collection.getDocument[StoredDocument]("33")
      yield
        entity.getOld should be(StoredDocument("33", "Angela"))
        notFound shouldBe empty
    }
  }

object CollectionIntegrationSpec:
  case class StoredDocument(_key: String, name: String)

  case class UserDocument(_key: String, name: String)

  case class UserUpdateDocument(name: String)

  case class UpdateDocument(name: String)

class CollectionIntegrationSpecWithTry extends CollectionIntegrationSpec[Try, Vector]

class CollectionIntegrationSpecWithEither extends CollectionIntegrationSpec[[X] =>> Either[Throwable, X], Vector]

class CollectionIntegrationSpecWithFuture extends CollectionIntegrationSpec[[X] =>> Future[X], Vector]

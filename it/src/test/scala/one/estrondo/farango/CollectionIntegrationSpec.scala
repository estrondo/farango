package one.estrondo.farango

import com.arangodb.model.CollectionCreateOptions
import com.arangodb.model.DocumentCreateOptions
import one.estrondo.farango.CollectionIntegrationSpec.StoredDocument
import one.estrondo.farango.CollectionIntegrationSpec.UserDocument
import org.scalatest.Assertion
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.language.implicitConversions
import scala.util.Try

abstract class CollectionIntegrationSpec[F[_]: Effect: EffectToFuture] extends FarangoIntegrationSpec[F]:

  protected def withCollection(block: Collection => F[Assertion]): Future[Assertion] =
    withDB { db =>
      for
        database   <- DB(db).db("test-database", true)
        collection <- database.collection("test-collection", Some(CollectionCreateOptions().waitForSync(true)))
        assertion  <- block(collection)
      yield assertion
    }

  given Transformer[UserDocument, StoredDocument] with
    override def apply[F[_]: Effect](a: UserDocument): F[StoredDocument] =
      Effect[F].succeed(StoredDocument(a._key, a.name))

  given Transformer[StoredDocument, UserDocument] with
    override def apply[F[_]: Effect](a: StoredDocument): F[UserDocument] =
      Effect[F].succeed(UserDocument(a._key, a.name))

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
  }

object CollectionIntegrationSpec:
  case class StoredDocument(_key: String, name: String)

  case class UserDocument(_key: String, name: String)

class CollectionIntegrationSpecWithTry extends CollectionIntegrationSpec[Try]

class CollectionIntegrationSpecWithEither extends CollectionIntegrationSpec[[X] =>> Either[Throwable, X]]

class CollectionIntegrationSpecWithFuture extends CollectionIntegrationSpec[[X] =>> Future[X]]

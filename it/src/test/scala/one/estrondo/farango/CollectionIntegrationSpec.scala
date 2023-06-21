package one.estrondo.farango

import com.arangodb.model.DocumentCreateOptions
import com.arangodb.model.DocumentDeleteOptions
import com.arangodb.model.DocumentUpdateOptions
import one.estrondo.farango.test.domain.DomainDocument
import one.estrondo.farango.test.domain.DomainDocumentFixture
import one.estrondo.farango.test.domain.KeyDocument
import one.estrondo.farango.test.domain.UpdateDomainDocument
import one.estrondo.farango.test.domain.given
import one.estrondo.farango.test.stored.StoredDocument
import one.estrondo.farango.test.stored.given
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.Try

abstract class CollectionIntegrationSpec[F[+_]: Effect: EffectToFuture, S[_]](using
    EffectStream[S, F],
    EffectStreamCollector[S, F]
) extends FarangoIntegrationSpec[F, S]:

  "A Collection instance" - {
    "It should insert a document." in withCollection() { collection =>
      val toInsert = DomainDocumentFixture.createNew()

      for entity <- collection.insertDocument[StoredDocument, KeyDocument](
                      toInsert,
                      DocumentCreateOptions().returnOld(true).returnNew(true)
                    )
      yield
        entity.getOld() shouldBe null
        entity.getNew() should be(KeyDocument(toInsert._key))
    }

    "It should get a document." in withCollection() { collection =>
      val toInsert = DomainDocumentFixture.createNew()

      for
        _      <- collection.insertDocument[StoredDocument, KeyDocument](toInsert)
        result <- collection.getDocument[StoredDocument, DomainDocument](toInsert._key)
      yield result should contain(toInsert)
    }

    "It should delete a document." in withCollection() { collection =>
      val toDelete = DomainDocumentFixture.createNew()
      for
        _      <- collection.insertDocument[StoredDocument, KeyDocument](toDelete)
        entity <-
          collection.deleteDocument[StoredDocument, KeyDocument](toDelete._key, DocumentDeleteOptions().returnOld(true))
      yield entity.getOld should be(KeyDocument(_key = toDelete._key))
    }

    "It should query for some documents." in withCollection() { collection =>
      val toInsert = (for (_ <- 0 until 10) yield DomainDocumentFixture.createNew()).toList

      for
        _      <- Effect[F].foreach(toInsert)(d => collection.insertDocument[StoredDocument, KeyDocument](d))
        result <- collection.database
                    .query[StoredDocument, DomainDocument](
                      "FOR d IN @@collection RETURN d",
                      Map("@collection" -> "test-collection")
                    )
                    .collect()
      yield result should contain theSameElementsAs toInsert
    }

    "It should update a document." in withCollection() { collection =>
      val originalDocument = DomainDocumentFixture.createNew()
      val randomDocument   = DomainDocumentFixture.createNew()
      val expectedDocument = originalDocument.copy(title = randomDocument.title, length = randomDocument.length)

      for
        _       <- collection.insertDocument[StoredDocument, DomainDocument](originalDocument)
        entity  <- collection.updateDocument[StoredDocument, UpdateDomainDocument, DomainDocument](
                     originalDocument._key,
                     randomDocument,
                     DocumentUpdateOptions().returnOld(true).returnNew(true)
                   )
        updated <- collection.getDocument[StoredDocument, DomainDocument](originalDocument._key)
      yield
        entity.getNew should be(expectedDocument)
        entity.getOld should be(originalDocument)
        updated should contain(expectedDocument)

    }
  }

abstract class SyncCollectionIntegrationSpec[F[+_]: Effect: EffectToFuture, S[_]](using
    EffectStream[S, F],
    EffectStreamCollector[S, F]
) extends CollectionIntegrationSpec[F, S],
      TestSyncDB

class SyncCollectionIntegrationSpecWithTry extends SyncCollectionIntegrationSpec[Try, Vector]

class SyncCollectionIntegrationSpecWithEither
    extends SyncCollectionIntegrationSpec[[X] =>> Either[Throwable, X], Vector]

class SyncCollectionIntegrationSpecWithFuture extends SyncCollectionIntegrationSpec[Future, Vector]

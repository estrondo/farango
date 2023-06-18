package one.estrondo.farango

import one.estrondo.farango.test.domain.DomainDocumentFixture
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
    "It should insert/get a document." in withCollection() { collection =>
      val toInsert = DomainDocumentFixture.createNew()

      for
        _      <- collection.insertDocument[StoredDocument](toInsert)
        result <- collection.getDocument[StoredDocument](toInsert._key)
      yield result should contain(toInsert)
    }

    "It should query for some documents." in withCollection() { collection =>
      val toInsert = (for (_ <- 0 until 10) yield DomainDocumentFixture.createNew()).toList

      for
        _      <- Effect[F].foreach(toInsert)(d => collection.insertDocument[StoredDocument](d))
        result <- collection.database
                    .query[StoredDocument]("FOR d IN @@collection RETURN d", Map("@collection" -> "test-collection"))
                    .collect()
      yield result should contain theSameElementsAs toInsert
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

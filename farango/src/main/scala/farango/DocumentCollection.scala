package farango

import com.arangodb.ArangoDBException
import com.arangodb.async.ArangoCollectionAsync
import com.arangodb.async.ArangoDatabaseAsync
import com.arangodb.entity.DocumentDeleteEntity
import com.arangodb.entity.DocumentUpdateEntity
import com.arangodb.entity.IndexEntity
import com.arangodb.model.CollectionCreateOptions
import com.arangodb.model.DocumentCreateOptions
import com.arangodb.model.DocumentDeleteOptions
import com.arangodb.model.DocumentUpdateOptions
import com.arangodb.model.GeoIndexOptions

import java.util.Collections
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletionException
import java.util.concurrent.CompletionStage
import scala.jdk.CollectionConverters.IterableHasAsJava
import scala.reflect.ClassTag

trait DocumentCollection:

  def database: Database

  def name: String

  def ensureGeoIndex[F[_]: Effect](fields: Iterable[String], options: GeoIndexOptions): F[IndexEntity]

  def documentsT[T: ClassTag, S[_]](using EffectStream[S, _]): S[T]

  def documents[T]: PartialDocumentDocuments[T] = new PartialDocumentDocuments(this)

  def getT[T: ClassTag, F[_]: Effect](key: String): F[Option[T]]

  def get[T]: PartialDocumentGet[T] = PartialDocumentGet(this)

  def insertT[T: ClassTag, F[_]: Effect](document: T): F[T]

  def insert[T]: PartialDocumentInsert[T] = PartialDocumentInsert(this)

  def removeT[T: ClassTag, F[_]: Effect](key: String): F[Option[T]]

  def remove[T]: PartialDocumentRemove[T] = PartialDocumentRemove(this)

  def updateT[U, T: ClassTag, F[_]: Effect](
      key: String,
      document: U,
      updateReturn: UpdateReturn = UpdateReturn.None
  ): F[Option[T]]

  def update[U, T]: PartialDocumentUpdate[U, T] = PartialDocumentUpdate(this)

object DocumentCollection:

  def apply[F[_]: Effect](
      name: String,
      database: Database
  ): F[DocumentCollection] =

    val collection = database.underlying.collection(name)
    val response   = collection.exists().thenComposeAsync { exists =>
      if exists then CompletableFuture.completedFuture(())
      else create(collection)
    }

    Effect[F].mapFromCompletionStage(response)(_ => DocumentCollectionImpl(database, collection))

  private[farango] def create(collection: ArangoCollectionAsync): CompletableFuture[Unit] =
    val options = CollectionCreateOptions()
      .waitForSync(true)
    collection.create(options).thenApply(_ => ())

private[farango] class DocumentCollectionImpl(
    val database: Database,
    collection: ArangoCollectionAsync
) extends DocumentCollection:

  override def name: String = collection.name()

  override def ensureGeoIndex[F[_]: Effect](fields: Iterable[String], options: GeoIndexOptions): F[IndexEntity] =
    Effect[F].mapFromCompletionStage(collection.ensureGeoIndex(fields.asJava, options))(identity)

  override def getT[T: ClassTag, F[_]: Effect](key: String): F[Option[T]] =
    Effect[F].mapFromCompletionStage(collection.getDocument(key, expectedType[T]))(Option(_))

  override def insertT[T: ClassTag, F[_]: Effect](document: T): F[T] =
    val options = DocumentCreateOptions()
      .returnNew(true)

    handle(Effect[F].mapFromCompletionStage(collection.insertDocument(document, options)) { entity =>
      entity.getNew()
    })

  override def documentsT[T: ClassTag, S[_]](using effect: EffectStream[S, _]): S[T] =
    val completionStage = database.underlying
      .query(
        "FOR e IN @@collection RETURN e",
        Collections.singletonMap("@collection", collection.name()),
        expectedType[T]
      )
      .thenApply(cursor => cursor.streamRemaining())

    effect.mapFromCompletionStage(completionStage)(identity)

  override def removeT[T: ClassTag, F[_]: Effect](key: String): F[Option[T]] =
    val options = DocumentDeleteOptions()
      .returnOld(true)

    val completionStage = collection
      .deleteDocument(key, expectedType, options)
      .exceptionallyCompose(alternativeEntity(DocumentDeleteEntity()))

    handle(Effect[F].mapFromCompletionStage(completionStage)(entity => Option(entity.getOld())))

  override def updateT[U, T: ClassTag, F[_]: Effect](
      key: String,
      document: U,
      updateReturn: UpdateReturn = UpdateReturn.None
  ): F[Option[T]] =
    val options = updateReturn.configure(DocumentUpdateOptions())

    val completionStage = collection
      .updateDocument(key, document, options, expectedType[T])
      .exceptionallyCompose(alternativeEntity(DocumentUpdateEntity()))

    handle(Effect[F].mapFromCompletionStage(completionStage)(entity => updateReturn(entity)))

  // https://www.arangodb.com/docs/3.10/appendix-error-codes.html

  private def handle[T, F[_]: Effect](effect: F[T]): F[T] =
    Effect[F].handleErrorWith(effect) {
      case cause: ArangoDBException if cause.getErrorNum() == 1203 =>
        tryRecreateCollection(effect)

      case other: Throwable =>
        Effect[F].failed(other)
    }

  private inline def expectedType[T](using tag: ClassTag[T]): Class[T] =
    tag.runtimeClass.asInstanceOf[Class[T]]

  private def alternativeEntity[T](entity: T)(throwable: Throwable): CompletionStage[T] =
    throwable match
      case wrapper: CompletionException =>
        alternativeEntity(entity)(wrapper.getCause())

      case cause: ArangoDBException if cause.getErrorNum() == 1202 =>
        CompletableFuture.completedFuture(entity)

      case _ => CompletableFuture.failedStage(throwable)

  private def tryRecreateCollection[T, F[_]: Effect](effect: F[T]): F[T] =
    Effect[F]
      .flatMapFromCompletionStage(DocumentCollection.create(collection))(_ => effect)

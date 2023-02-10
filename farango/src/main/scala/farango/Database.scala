package farango

import com.arangodb.async.ArangoDBAsync
import com.arangodb.async.ArangoDatabaseAsync

import scala.jdk.CollectionConverters.MapHasAsJava
import scala.reflect.ClassTag
import java.util.concurrent.CompletableFuture

trait Database:

  def queryT[T: ClassTag, F[_]: Effect, S[_]](
      query: String,
      parameters: Map[String, Any] = Map.empty
  )(using EffectStream[S, F]): F[S[T]]

  def query[T]: PartialDatabaseQuery[T] = PartialDatabaseQuery(this)

  def documentCollection[F[_]: Effect](name: String): F[DocumentCollection]

  private[farango] def underlying: ArangoDatabaseAsync

object Database:

  def apply[F[_]: Effect](server: ArangoDBAsync, name: String): F[Database] =
    apply(server.db(name))

  def apply[F[_]: Effect](database: ArangoDatabaseAsync): F[Database] =
    val creation = database
      .exists()
      .thenComposeAsync({ exists =>
        if exists then CompletableFuture.completedStage(java.lang.Boolean.TRUE)
        else database.create()
      })

    Effect[F].mapFromCompletionStage(creation)(_ => FarangoDatabaseImpl(database))

private[farango] class FarangoDatabaseImpl(override private[farango] val underlying: ArangoDatabaseAsync)
    extends Database:

  override def documentCollection[F[_]: Effect](name: String): F[DocumentCollection] =
    DocumentCollection(name, this)

  def queryT[T: ClassTag, F[_]: Effect, S[_]](
      query: String,
      parameters: Map[String, Any] = Map.empty
  )(using EffectStream[S, F]): F[S[T]] =
    Effect[F].mapFromCompletionStage(
      underlying.query(query, parameters.asJava, expectedClass)
    ) { cursor =>
      EffectStream[S, F].mapFromJavaStream(cursor.streamRemaining())(identity)
    }

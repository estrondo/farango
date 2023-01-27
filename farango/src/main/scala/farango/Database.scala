package farango

import com.arangodb.async.ArangoDBAsync
import com.arangodb.async.ArangoDatabaseAsync

import scala.jdk.CollectionConverters.MapHasAsJava
import scala.reflect.ClassTag

trait Database:

  def queryT[T: ClassTag, F[_]: Effect, S[_]](
      query: String,
      parameters: Map[String, Any] = Map.empty
  )(using EffectStream[S, F]): F[S[T]]

  def query[T]: PartialDatabaseQuery[T] = PartialDatabaseQuery(this)

  def documentCollection[F[_]: Effect](name: String): F[DocumentCollection]

  private[farango] def underlying: ArangoDatabaseAsync

object Database:

  def apply(server: ArangoDBAsync, name: String): Database =
    apply(server.db(name))

  def apply(database: ArangoDatabaseAsync): Database =
    FarangoDatabaseImpl(database)

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

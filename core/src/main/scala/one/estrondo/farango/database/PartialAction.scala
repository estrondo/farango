package one.estrondo.farango.database

import com.arangodb.model.AqlQueryOptions
import java.util
import java.util.stream
import one.estrondo.farango.Composed
import one.estrondo.farango.Effect
import one.estrondo.farango.EffectStream
import one.estrondo.farango.EffectStreamOps.map
import one.estrondo.farango.Transformer
import scala.jdk.CollectionConverters.MapHasAsJava
import scala.reflect.ClassTag

//noinspection ScalaFileName
trait PartialQuery[A, R] extends Composed:

  def apply[F[_]: Effect, S[_]](
      query: String,
      bindVars: Map[String, AnyRef] = Map.empty,
      options: AqlQueryOptions = AqlQueryOptions()
  )(using EffectStream[S, F], Transformer[A, R], ClassTag[A]): S[R] =
    EffectStream[S, F]
      .fromJavaStream(compose(search(query, bindVars.asJava, options)))
      .map(Transformer[A, R].apply)

  protected def search(query: String, bindVars: util.Map[String, Object], options: AqlQueryOptions)(using
      ClassTag[A]
  ): G[stream.Stream[A]]

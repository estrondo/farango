package one.estrondo.farango.database

import com.arangodb.ArangoDatabase
import com.arangodb.model.AqlQueryOptions
import one.estrondo.farango.Effect
import one.estrondo.farango.StreamEffect
import one.estrondo.farango.Transformer
import scala.jdk.CollectionConverters.MapHasAsJava
import scala.reflect.ClassTag

class PartiallyAppliedQuery[A](arango: ArangoDatabase):

  def apply[T, S[_], F[_]](
      query: String,
      bindVars: Map[String, AnyRef] = Map.empty,
      options: AqlQueryOptions = AqlQueryOptions()
  )(using Transformer[A, T], StreamEffect[S, F], Effect[F], ClassTag[A]): S[T] =
    StreamEffect[S, F].mapEffect(StreamEffect[S, F].fromEffect {
      Effect[F].attemptBlocking {
        arango
          .query(query, summon[ClassTag[A]].runtimeClass.asInstanceOf[Class[A]], bindVars.asJava, options)
          .stream()
      }
    })(Transformer[A, T].apply)

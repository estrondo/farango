package farango

import scala.reflect.ClassTag

class PartialDatabaseQuery[T](database: Database):

  def apply[O, F[_]: Effect, S[_]](query: String, parameters: Map[String, Any] = Map.empty)(using
      ClassTag[T],
      EffectConversion[T, O, F],
      EffectStream[S, F]
  ): F[S[O]] =
    val streamingEffect = database.query[T, F, S](query, parameters)
    Effect[F]
      .map(streamingEffect)(stream => EffectStream[S, F].effectMap(stream)(summon[EffectConversion[T, O, F]].apply))

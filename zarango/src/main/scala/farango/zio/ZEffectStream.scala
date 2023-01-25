package farango.zio

import farango.Effect
import zio.ZIO
import zio.stream.ZStream
import java.util.concurrent.CompletionStage
import farango.EffectStream
import java.util.stream

type ZEffectStream[A] = ZStream[Any, Throwable, A]

given ZEffectStream: EffectStream[ZEffectStream, ZEffect] = new EffectStream:

  override def effectMap[A, B](stream: ZStream[Any, Throwable, A])(fn: A => ZEffect[B]): ZStream[Any, Throwable, B] =
    stream.mapZIO(fn)

  override def mapFromCompletionStage[A, B](stream: => CompletionStage[JavaStream[A]])(
      fn: A => B
  ): ZStream[Any, Throwable, B] =
    ZStream
      .fromJavaStreamZIO(ZIO.fromCompletionStage(stream))
      .map(fn)

  override def mapFromJavaStream[A, B](stream: => JavaStream[A])(fn: A => B): ZStream[Any, Throwable, B] =
    ZStream
      .fromJavaStream(stream)
      .map(fn)

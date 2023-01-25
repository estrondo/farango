package farango.zio

import zio.ZIO
import farango.Effect
import java.util.concurrent.CompletionStage

type ZEffect[A] = ZIO[Any, Throwable, A]

given ZEffect: Effect[ZEffect] = new Effect:

  override def map[A, B](a: ZIO[Any, Throwable, A])(fn: A => B): ZIO[Any, Throwable, B] =
    a.map(fn)

  override def flatMap[A, B](a: ZIO[Any, Throwable, A])(fn: A => ZIO[Any, Throwable, B]): ZIO[Any, Throwable, B] =
    a.flatMap(fn)

  override def flatMapFromCompletionStage[A, B](a: => CompletionStage[A])(
      fn: A => ZIO[Any, Throwable, B]
  ): ZIO[Any, Throwable, B] =
    ZIO
      .fromCompletionStage(a)
      .flatMap(fn)

  override def mapFromCompletionStage[A, B](a: => CompletionStage[A])(fn: A => B): ZIO[Any, Throwable, B] =
    ZIO
      .fromCompletionStage(a)
      .map(fn)

  override def none[A]: ZIO[Any, Throwable, Option[A]] =
    ZIO.none

  override def succeed[A](a: => A): ZIO[Any, Throwable, A] =
    ZIO.succeed(a)

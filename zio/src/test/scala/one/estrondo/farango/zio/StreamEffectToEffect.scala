package one.estrondo.farango.zio

import one.estrondo.farango.Effect
import one.estrondo.farango.StreamEffectToEffect
import zio.Runtime
import zio.Unsafe
import zio.stream.ZStream

//noinspection ScalaFileName
given StreamEffectToEffect[[X] =>> ZStream[Any, Throwable, X]] with

  override def collect[A, F[_]: Effect](stream: ZStream[Any, Throwable, A]): F[Iterable[A]] =
    Effect[F].attempt {
      Unsafe.unsafe { implicit unsafe =>
        Runtime.default.unsafe.run(stream.runCollect).getOrThrow()
      }
    }

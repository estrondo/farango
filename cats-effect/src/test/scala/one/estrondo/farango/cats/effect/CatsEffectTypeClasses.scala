package one.estrondo.farango.cats.effect

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import one.estrondo.farango.EffectStreamCollector
import one.estrondo.farango.EffectToFuture
import scala.concurrent.Future

//noinspection ScalaFileName
given EffectToFuture[IO] with

  override def apply[A](a: IO[A]): Future[A] = a.unsafeToFuture()

given EffectStreamCollector[[O] =>> fs2.Stream[IO, O], IO] with

  override def apply[A](a: fs2.Stream[IO, A]): IO[Seq[A]] =
    a.compile.toList

package one.estrondo.farango.cats.effect

import cats.effect.IO
import java.util.stream
import one.estrondo.farango.Effect
import one.estrondo.farango.EffectStream
import scala.jdk.CollectionConverters.IteratorHasAsScala
import scala.util.Failure
import scala.util.Success
import scala.util.Try

//noinspection ScalaFileName
given Effect[IO] with

  override def flatMap[A, B](a: IO[A])(f: A => IO[B]): IO[B] = a.flatMap(f)

  override def fromBlockingTry[A](a: => Try[A]): IO[A] =
    IO.blocking(a).flatMap {
      case Success(value) => IO.pure(value)
      case Failure(cause) => IO.raiseError(cause)
    }

  override def fromTry[A](a: => Try[A]): IO[A] =
    IO.fromTry(a)

  override def map[A, B](a: IO[A])(f: A => B): IO[B] =
    a.map(f)

  override def unit: IO[Unit] = IO.unit

  override def none: IO[Option[Nothing]] = IO.none

given EffectStream[[O] =>> fs2.Stream[IO, O], IO] with

  override def fromJavaStream[A](a: IO[stream.Stream[A]]): fs2.Stream[IO, A] =
    fs2.Stream
      .eval(a)
      .flatMap(javaStream => fs2.Stream.fromBlockingIterator(javaStream.iterator().asScala, 32))

  override def map[A, B](a: fs2.Stream[IO, A])(f: A => IO[B]): fs2.Stream[IO, B] =
    a.evalMap(f)

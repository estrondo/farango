package one.estrondo.farango.zio

import java.util.stream
import one.estrondo.farango.Effect
import one.estrondo.farango.StreamEffect
import zio.Task
import zio.ZIO
import zio.stream.ZStream

//noinspection ScalaFileName
given Effect[[X] =>> Task[X]] with

  override def attempt[A](value: => A): Task[A] =
    ZIO.attempt(value)

  override def attemptBlocking[A](value: => A): Task[A] =
    ZIO.attemptBlocking(value)

  override def fail[A](cause: => Throwable): Task[A] =
    ZIO.fail(cause)

  override def flatMap[A, B](a: Task[A])(f: A => Task[B]): Task[B] =
    a.flatMap(f)

  override def map[A, B](a: Task[A])(f: A => B): Task[B] =
    a.map(f)

  override def succeed[A](value: => A): Task[A] =
    ZIO.succeed(value)

given StreamEffect[[X] =>> ZStream[Any, Throwable, X], [Y] =>> Task[Y]] with

  override def fromEffect[A](a: => Task[stream.Stream[A]]): ZStream[Any, Throwable, A] =
    ZStream.fromJavaStreamZIO(a)

  override def mapEffect[A, B](a: ZStream[Any, Throwable, A])(f: A => Task[B]): ZStream[Any, Throwable, B] =
    a.mapZIO(f)

package one.estrondo.farango.zio

import java.util.stream
import one.estrondo.farango.Effect
import one.estrondo.farango.EffectStream
import scala.util.Try
import zio.Task
import zio.ZIO
import zio.stream.ZStream

//noinspection ScalaFileName
given Effect[Task] with

  override def flatMap[A, B](a: Task[A])(f: A => Task[B]): Task[B] =
    a.flatMap(f)

  override def fromBlockingTry[A](a: => Try[A]): Task[A] =
    ZIO.blocking(ZIO.fromTry(a))

  override def fromTry[A](a: => Try[A]): Task[A] =
    ZIO.fromTry(a)

  override def map[A, B](a: Task[A])(f: A => B): Task[B] =
    a.map(f)

  override def unit: Task[Unit] =
    ZIO.unit

  override def none: Task[Option[Nothing]] =
    ZIO.none

given EffectStream[[X] =>> ZStream[Any, Throwable, X], Task] with

  override def fromJavaStream[A](a: Task[stream.Stream[A]]): ZStream[Any, Throwable, A] =
    ZStream.fromJavaStreamZIO(a)

  override def map[A, B](a: ZStream[Any, Throwable, A])(f: A => Task[B]): ZStream[Any, Throwable, B] =
    a.mapZIO(f)

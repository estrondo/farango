package one.estrondo.farango.zio

import one.estrondo.farango.Effect
import zio.Task
import zio.ZIO

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

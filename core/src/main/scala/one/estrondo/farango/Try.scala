package one.estrondo.farango

import scala.util.Failure
import scala.util.Success
import scala.util.Try

//noinspection ScalaFileName
given Effect[Try] with

  override def attempt[A](value: => A): Try[A] =
    Try(value)

  override def attemptBlocking[A](value: => A): Try[A] =
    Try(value)

  override def fail[A](cause: => Throwable): Try[A] =
    Failure(cause)

  override def flatMap[A, B](a: Try[A])(f: A => Try[B]): Try[B] =
    a.flatMap(f)

  override def map[A, B](a: Try[A])(f: A => B): Try[B] =
    a.map(f)

  override def succeed[A](value: => A): Try[A] =
    Success(value)

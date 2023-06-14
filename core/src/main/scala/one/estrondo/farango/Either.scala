package one.estrondo.farango

import java.util.stream
import scala.util.Failure
import scala.util.Success
import scala.util.Try

//noinspection ScalaFileName
given Effect[[X] =>> Either[Throwable, X]] with

  override def attemptBlocking[A](value: => A): Either[Throwable, A] =
    attempt(value)

  override def attempt[A](value: => A): Either[Throwable, A] =
    try Right(value)
    catch case error: Throwable => Left(error)

  override def fail[A](cause: => Throwable): Either[Throwable, A] =
    Left(cause)

  override def flatMap[A, B](a: Either[Throwable, A])(f: A => Either[Throwable, B]): Either[Throwable, B] =
    a.flatMap(f)

  override def succeed[A](value: => A): Either[Throwable, A] =
    Right(value)

  override def map[A, B](a: Either[Throwable, A])(f: A => B): Either[Throwable, B] =
    a.map(f)

given [I[_] <: Iterable[_]](using other: StreamEffect[I, Try]): StreamEffect[I, [X] =>> Either[Throwable, X]] with

  override def fromEffect[A](a: => Either[Throwable, stream.Stream[A]]): I[A] =
    other.fromEffect(a match
      case Right(value) => Success(value)
      case Left(cause)  => Failure(cause)
    )

  override def mapEffect[A, B](a: I[A])(f: A => Either[Throwable, B]): I[B] =
    other.mapEffect(a)(va => {
      f(va) match
        case Right(value) => Success(value)
        case Left(cause)  => Failure(cause)
    })

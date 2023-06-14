package one.estrondo.farango

import java.util.stream
import scala.concurrent.Await
import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import scala.concurrent.duration.Duration
import scala.util.Try

//noinspection ScalaFileName
given (using ExecutionContext): Effect[[X] =>> Future[X]] with

  override def attempt[A](value: => A): Future[A] =
    Future(value)

  override def attemptBlocking[A](value: => A): Future[A] =
    Future(value)

  override def fail[A](cause: => Throwable): Future[A] =
    Future.failed(cause)

  override def flatMap[A, B](a: Future[A])(f: A => Future[B]): Future[B] =
    a.flatMap(f)

  override def map[A, B](a: Future[A])(f: A => B): Future[B] =
    a.map(f)

  override def succeed[A](value: => A): Future[A] =
    Future.successful(value)

given [I[_] <: Iterable[_]](using other: StreamEffect[I, Try]): StreamEffect[I, [X] =>> Future[X]] with

  override def fromEffect[A](a: => Future[stream.Stream[A]]): I[A] =
    other.fromEffect(Try(Await.result(a, Duration.Inf)))

  override def mapEffect[A, B](a: I[A])(f: A => Future[B]): I[B] =
    other.mapEffect(a) { va =>
      Try(Await.result(f(va), Duration.Inf))
    }

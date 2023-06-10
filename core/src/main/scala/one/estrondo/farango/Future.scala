package one.estrondo.farango

import scala.concurrent.ExecutionContext
import scala.concurrent.Future

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

package one.estrondo.farango

import scala.collection.Factory
import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import scala.util.Success
import scala.util.Try

trait Effect[F[_]]:

  def flatMap[A, B](a: F[A])(f: A => F[B]): F[B]

  def foreach[A, B, I[_] <: Iterable[_]](a: I[A])(f: A => F[B])(using Factory[B, I[B]]): F[I[B]] =
    val iterator = a.iterator.asInstanceOf[Iterator[A]]
    val builder  = summon[Factory[B, I[B]]].newBuilder

    def add(b: B): F[I[B]] =
      builder.addOne(b)
      next(iterator.nextOption())

    def next(value: Option[A]): F[I[B]] = value match
      case Some(next) => flatMap(f(next))(add)
      case None       => fromTry(Try(builder.result()))

    flatMap(fromTry(Try(iterator.nextOption())))(next)

  def fromBlockingTry[A](a: => Try[A]): F[A]

  def fromTry[A](a: => Try[A]): F[A]

  def map[A, B](a: F[A])(f: A => B): F[B]

  def unit: F[Unit]

  def none: F[Option[Nothing]]

object Effect:

  inline def apply[F[_]](using inline effect: Effect[F]): Effect[F] = effect

given Effect[Try] with

  override def flatMap[A, B](a: Try[A])(f: A => Try[B]): Try[B] = a.flatMap(f)

  override def fromBlockingTry[A](a: => Try[A]): Try[A] = a

  override def fromTry[A](a: => Try[A]): Try[A] = a

  override def map[A, B](a: Try[A])(f: A => B): Try[B] = a.map(f)

  override def unit: Try[Unit] = Success(())

  override def none: Try[Option[Nothing]] = Try(None)

given Effect[[X] =>> Either[Throwable, X]] with

  override def flatMap[A, B](a: Either[Throwable, A])(f: A => Either[Throwable, B]): Either[Throwable, B] = a.flatMap(f)

  override def fromBlockingTry[A](a: => Try[A]): Either[Throwable, A] = a.toEither

  override def fromTry[A](a: => Try[A]): Either[Throwable, A] = a.toEither

  override def map[A, B](a: Either[Throwable, A])(f: A => B): Either[Throwable, B] = a.map(f)

  override def unit: Either[Throwable, Unit] = Right(())

  override def none: Either[Throwable, Option[Nothing]] = Right(None)

given (using ExecutionContext): Effect[Future] with

  override def flatMap[A, B](a: Future[A])(f: A => Future[B]): Future[B] = a.flatMap(f)

  override def fromBlockingTry[A](a: => Try[A]): Future[A] = Future.fromTry(a)

  override def fromTry[A](a: => Try[A]): Future[A] = Future.fromTry(a)

  override def map[A, B](a: Future[A])(f: A => B): Future[B] = a.map(f)

  override def unit: Future[Unit] = Future.unit

  override def none: Future[Option[Nothing]] = Future.successful(None)

object EffectOps:

  extension [A, F[_]: Effect](a: F[A])

    def flatMap[B](f: A => F[B]): F[B] =
      Effect[F].flatMap(a)(f)

    def map[B](f: A => B): F[B] =
      Effect[F].map(a)(f)

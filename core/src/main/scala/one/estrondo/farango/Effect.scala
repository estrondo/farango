package one.estrondo.farango

import scala.collection.Factory

trait Effect[F[_]]:

  def attempt[A](value: => A): F[A]

  def attemptBlocking[A](value: => A): F[A]

  def fail[A](cause: => Throwable): F[A]

  def foreach[A, B, I[+X] <: Iterable[X]](iterable: I[A])(f: A => F[B])(using
      factory: Factory[B, I[B]]
  ): F[I[B]] =
    val builder  = factory.newBuilder
    val iterator = iterable.iterator

    def next(a: Option[A]): F[I[B]] = a match
      case Some(value) =>
        flatMap(f(value)) { b =>
          builder.addOne(b)
          next(iterator.nextOption())
        }

      case None => succeed(builder.result())

    flatMap(attempt(iterator.nextOption()))(next)

  def flatMap[A, B](a: F[A])(f: A => F[B]): F[B]

  def map[A, B](a: F[A])(f: A => B): F[B]

  def mapOption[A, B](a: F[Option[A]])(f: A => F[B]): F[Option[B]] =
    flatMap(a) {
      case Some(value) => map(f(value))(Option.apply)
      case None        => succeed(None)
    }

  def succeed[A](value: => A): F[A]

object Effect:
  inline def apply[F[_]](using inline effect: Effect[F]): Effect[F] = effect

object EffectOps:
  extension [A, F[_]: Effect](a: F[A])

    def map[B](f: A => B): F[B] =
      Effect[F].map(a)(f)

    def flatMap[B](f: A => F[B]): F[B] =
      Effect[F].flatMap(a)(f)

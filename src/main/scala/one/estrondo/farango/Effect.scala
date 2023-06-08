package one.estrondo.farango

trait Effect[F[_]]:

  def attempt[A](value: => A): F[A]

  def fail[A](cause: => Throwable): F[A]

  def flatMap[A, B](a: F[A], fn: A => F[B]): F[B]

  def map[A, B](a: F[A], fn: A => B): F[B]

  def succeed[A](value: => A): F[A]

object Effect:
  inline def apply[F[_]](using inline effect: Effect[F]): Effect[F] = effect

extension [A, F[_]: Effect](a: F[A])

  def map[B](fn: A => B): F[B] =
    Effect[F].map(a, fn)

  def flatMap[B](fn: A => F[B]): F[B] =
    Effect[F].flatMap(a, fn)

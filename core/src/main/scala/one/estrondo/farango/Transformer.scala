package one.estrondo.farango

import scala.util.Try

trait Transformer[A, B]:

  def fromOption[F[+_]: Effect](option: Option[A]): F[Option[B]] =
    option match
      case Some(value) => Effect[F].fromTry(Try(Some(transform(value))))
      case None        => Effect[F].none

  def apply[F[_]: Effect](value: A): F[B] =
    Effect[F].fromTry(Try(transform(value)))

  def transform(value: A): B

object Transformer:

  inline def apply[A, B](using Transformer[A, B]): Transformer[A, B] =
    summon[Transformer[A, B]]

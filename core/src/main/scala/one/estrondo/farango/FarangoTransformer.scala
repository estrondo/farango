package one.estrondo.farango

import scala.annotation.implicitNotFound
import scala.util.Try

@implicitNotFound("There is no FarangoTransformer from ${A} to ${B}.")
trait FarangoTransformer[A, B]:

  def fromOption[F[+_]: Effect](option: Option[A]): F[Option[B]] =
    option match
      case Some(value) => Effect[F].fromTry(Try(Some(transform(value))))
      case None        => Effect[F].none

  def apply[F[_]: Effect](value: A): F[B] =
    Effect[F].fromTry(Try(transform(value)))

  def transform(value: A): B

object FarangoTransformer:

  inline def apply[A, B](using FarangoTransformer[A, B]): FarangoTransformer[A, B] =
    summon[FarangoTransformer[A, B]]

  given [A]: FarangoTransformer[A, A] with

    override def transform(value: A): A = value

package farango

import java.util.concurrent.CompletionStage

object Effect:

  transparent inline def apply[F[_]: Effect]: Effect[F] =
    summon[Effect[F]]

trait Effect[F[_]]:

  def handleErrorWith[A](a: F[A])(fn: Throwable => F[A]): F[A]

  def map[A, B](a: F[A])(fn: A => B): F[B]

  def mapFromCompletionStage[A, B](a: => CompletionStage[A])(fn: A => B): F[B]

  def flatMap[A, B](a: F[A])(fn: A => F[B]): F[B]

  def flatMapFromCompletionStage[A, B](a: => CompletionStage[A])(fn: A => F[B]): F[B]

  def succeed[A](a: => A): F[A]

  def failed[A](cause: Throwable): F[A]

  def none[A]: F[Option[A]]

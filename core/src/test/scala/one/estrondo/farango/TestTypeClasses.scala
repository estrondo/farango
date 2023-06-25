package one.estrondo.farango

import scala.concurrent.Future

trait EffectToFuture[F[_]]:
  def apply[A](a: F[A]): Future[A]

trait EffectStreamCollector[S[_], F[_]]:

  def apply[A](a: S[A]): F[Seq[A]]

trait Extractor[F[_]]:

  def get[A](a: F[A]): A

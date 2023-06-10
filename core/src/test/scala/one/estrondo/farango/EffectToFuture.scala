package one.estrondo.farango

import org.scalatest.Assertion
import scala.concurrent.Future
import scala.util.Try

trait EffectToFuture[F[_]]:

  def toFuture(a: F[Assertion]): Future[Assertion]

given EffectToFuture[Try] with

  override def toFuture(a: Try[Assertion]): Future[Assertion] =
    Future.fromTry(a)

given EffectToFuture[[X] =>> Either[Throwable, X]] with

  override def toFuture(a: Either[Throwable, Assertion]): Future[Assertion] =
    Future.fromTry(a.toTry)

given EffectToFuture[[X] =>> Future[X]] with

  override def toFuture(a: Future[Assertion]): Future[Assertion] = a

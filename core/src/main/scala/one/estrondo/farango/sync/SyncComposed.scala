package one.estrondo.farango.sync

import one.estrondo.farango.Composed
import one.estrondo.farango.Effect
import scala.util.Try

trait SyncComposed extends Composed:

  override type G[X] = Try[X]

  override protected def blockingCompose[A, F[_]: Effect](a: Try[A]): F[A] =
    Effect[F].fromBlockingTry(a)

  override protected def compose[A, F[_]: Effect](a: Try[A]): F[A] =
    Effect[F].fromTry(a)

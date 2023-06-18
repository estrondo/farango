package one.estrondo.farango

trait Composed:

  type G[_]

  protected def blockingCompose[A, F[_]: Effect](a: G[A]): F[A]

  protected def compose[A, F[_]: Effect](a: G[A]): F[A]

package one.estrondo.farango

trait Transformer[A, B]:

  def apply[F[_]: Effect](a: A): F[B]

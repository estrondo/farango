package one.estrondo.farango

trait Transformer[A, B]:

  def apply[F[_]: Effect](a: A): F[B]

object Transformer:

  inline def apply[A, B](using inline transformer: Transformer[A, B]): Transformer[A, B] =
    transformer

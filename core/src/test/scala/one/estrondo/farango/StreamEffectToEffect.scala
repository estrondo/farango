package one.estrondo.farango

trait StreamEffectToEffect[S[_]]:

  def collect[A, F[_]: Effect](stream: S[A]): F[Iterable[A]]

given StreamEffectToEffect[Vector] with

  override def collect[A, F[_]: Effect](stream: Vector[A]): F[Iterable[A]] =
    Effect[F].attempt {
      stream
    }

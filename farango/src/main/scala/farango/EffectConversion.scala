package farango

object EffectConversion:

  given [A, B, F[_]: Effect](using Conversion[A, B]): EffectConversion[A, B, F] = new EffectConversion:

    override def apply(a: A): F[B] = Effect[F].succeed(summon[Conversion[A, B]](a))

abstract class EffectConversion[A, B, F[_]] extends Conversion[A, F[B]]

package farango

object EffectConversion:

  given [A, B, F[_]](using Conversion[A, B], Effect[F]): EffectConversion[A, B, F] = new EffectConversion:

    override def apply(a: A): F[B] = Effect[F].succeed(summon[Conversion[A, B]](a))

abstract class EffectConversion[A, B, F[_]] extends Conversion[A, F[B]]

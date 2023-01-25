package farango

import scala.reflect.ClassTag

class PartialDocumentInsert[T](collection: DocumentCollection):

  def apply[I, F[_]: Effect](document: I)(using ClassTag[T], EffectConversion[I, T, F]): F[I] =
    val conversionEffect = summon[EffectConversion[I, T, F]](document)
    val insertiongEffect = Effect[F]
      .flatMap(conversionEffect)(toInsert => collection.insert[T, F](toInsert))

    Effect[F].map(insertiongEffect)(_ => document)

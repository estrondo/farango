package farango

import scala.reflect.ClassTag

class PartialDocumentUpdate[U, T](collection: DocumentCollection):

  def apply[I, O, F[_]: Effect](key: String, document: I, updateReturn: UpdateReturn = UpdateReturn.None)(using
      ClassTag[T],
      EffectConversion[I, U, F],
      EffectConversion[T, O, F]
  ): F[Option[O]] =
    val convertingEffect = summon[EffectConversion[I, U, F]](document)
    val updatingEffect   = Effect[F]
      .flatMap(convertingEffect)(toUpdate => collection.update[U, T, F](key, toUpdate, updateReturn))

    Effect[F]
      .flatMap(updatingEffect)({
        case Some(value) => Effect[F].map(summon[EffectConversion[T, O, F]](value))(Option.apply)
        case None        => Effect[F].none
      })

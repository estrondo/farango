package farango

import scala.reflect.ClassTag

class PartialDocumentRemove[T](collection: DocumentCollection):

  def apply[O, F[_]: Effect](key: String)(using ClassTag[T], EffectConversion[T, O, F]): F[Option[O]] =
    val removingEffect = collection.removeT[T, F](key)
    Effect[F]
      .flatMap(removingEffect)({
        case Some(value) => Effect[F].map(summon[EffectConversion[T, O, F]](value))(Option.apply)
        case None        => Effect[F].none
      })

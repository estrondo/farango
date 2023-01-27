package farango

import scala.reflect.ClassTag

class PartialDocumentGet[T](collection: DocumentCollection):

  def apply[O, F[_]: Effect](key: String)(using ClassTag[T], EffectConversion[T, O, F]): F[Option[O]] =
    val gettingEffect = collection.getT[T, F](key)
    Effect[F]
      .flatMap(gettingEffect)({
        case Some(value) => Effect[F].map(summon[EffectConversion[T, O, F]](value))(Option.apply)
        case None        => Effect[F].none
      })

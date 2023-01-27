package farango

import scala.reflect.ClassTag

class PartialDocumentDocuments[T](collection: DocumentCollection):

  def apply[O, S[_], F[_]]()(using EffectConversion[T, O, F], EffectStream[S, F], ClassTag[T]): S[O] =
    EffectStream[S, F]
      .effectMap(collection.documentsT[T, S])(summon[EffectConversion[T, O, F]])

package one.estrondo.farango.collection

import com.arangodb.ArangoCollection
import com.arangodb.entity.DocumentDeleteEntity
import com.arangodb.model.DocumentDeleteOptions
import one.estrondo.farango.Effect
import one.estrondo.farango.Transformer
import one.estrondo.farango.flatMap
import one.estrondo.farango.map
import scala.reflect.ClassTag

class PartiallyAppliedDeleteDocument[S](arango: ArangoCollection):

  def apply[T, F[_]: Effect](
      key: String,
      options: DocumentDeleteOptions = DocumentDeleteOptions()
  )(using transformer: Transformer[S, T], classTag: ClassTag[S]): F[(DocumentDeleteEntity[S], Option[T])] =
    for
      entity      <- Effect[F].attemptBlocking {
                       arango.deleteDocument(key, options, classTag.runtimeClass.asInstanceOf[Class[S]])
                     }
      oldDocument <- entity.getOld match
                       case null     => Effect[F].succeed(None)
                       case document => Effect[F].map(transformer(document))(Option.apply)
    yield entity -> oldDocument

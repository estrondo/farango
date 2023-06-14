package one.estrondo.farango.collection

import com.arangodb.ArangoCollection
import com.arangodb.entity.DocumentDeleteEntity
import com.arangodb.model.DocumentDeleteOptions
import one.estrondo.farango.Effect
import one.estrondo.farango.EffectOps.map
import scala.reflect.ClassTag

class PartiallyAppliedDeleteDocument[S](arango: ArangoCollection):

  def apply[F[_]: Effect](
      key: String,
      options: DocumentDeleteOptions = DocumentDeleteOptions()
  )(using classTag: ClassTag[S]): F[DocumentDeleteEntity[S]] =
    for entity <- Effect[F].attemptBlocking {
                    arango.deleteDocument(key, options, classTag.runtimeClass.asInstanceOf[Class[S]])
                  }
    yield entity

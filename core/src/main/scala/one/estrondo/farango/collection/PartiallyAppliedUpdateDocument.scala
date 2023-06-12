package one.estrondo.farango.collection

import com.arangodb.ArangoCollection
import com.arangodb.entity.DocumentUpdateEntity
import com.arangodb.model.DocumentUpdateOptions
import one.estrondo.farango.Effect
import one.estrondo.farango.Transformer
import one.estrondo.farango.flatMap
import one.estrondo.farango.map
import scala.reflect.ClassTag

class PartiallyAppliedUpdateDocument[S, U](arango: ArangoCollection):

  def apply[T, F[_]: Effect](key: String, value: T, options: DocumentUpdateOptions = DocumentUpdateOptions())(using
      transformer: Transformer[T, U],
      classTag: ClassTag[S]
  ): F[DocumentUpdateEntity[S]] =
    for
      toUpdate <- transformer(value)
      entity   <- Effect[F].attemptBlocking {
                    arango.updateDocument(key, toUpdate, options, classTag.runtimeClass.asInstanceOf[Class[S]])
                  }
    yield entity

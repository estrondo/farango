package one.estrondo.farango.collection

import com.arangodb.ArangoCollection
import com.arangodb.entity.DocumentCreateEntity
import com.arangodb.model.DocumentCreateOptions
import one.estrondo.farango.Effect
import one.estrondo.farango.Transformer
import one.estrondo.farango.flatMap
import one.estrondo.farango.map
import scala.reflect.ClassTag

class PartiallyAppliedInsertDocument[S](arango: ArangoCollection):

  def apply[T, F[_]: Effect](value: T, options: DocumentCreateOptions = DocumentCreateOptions())(using
      transformer: Transformer[T, S],
      classTag: ClassTag[S]
  ): F[DocumentCreateEntity[S]] =
    for
      transformed <- transformer(value)
      entity      <- Effect[F].attemptBlocking {
                       arango.insertDocument(transformed, options, classTag.runtimeClass.asInstanceOf[Class[S]])
                     }
    yield entity

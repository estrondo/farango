package one.estrondo.farango.collection

import com.arangodb.ArangoCollection
import com.arangodb.model.DocumentReadOptions
import one.estrondo.farango.Effect
import one.estrondo.farango.Transformer
import scala.reflect.ClassTag

class PartiallyAppliedGetDocument[S](arango: ArangoCollection):

  def apply[T, F[_]: Effect](key: String, options: DocumentReadOptions = DocumentReadOptions())(using
      transformer: Transformer[S, T],
      classTag: ClassTag[S]
  ): F[Option[T]] =
    Effect[F].mapOption(Effect[F].attemptBlocking {
      Option(arango.getDocument(key, classTag.runtimeClass.asInstanceOf[Class[S]], options))
    })(transformer.apply)

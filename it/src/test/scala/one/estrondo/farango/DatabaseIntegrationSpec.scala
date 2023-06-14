package one.estrondo.farango

import DatabaseIntegrationSpec.given
import one.estrondo.farango.DatabaseIntegrationSpec.StoredDocument
import one.estrondo.farango.DatabaseIntegrationSpec.UserDocument
import one.estrondo.farango.EffectOps.flatMap
import one.estrondo.farango.EffectOps.map
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.Try

abstract class DatabaseIntegrationSpec[F[_]: Effect: EffectToFuture, S[_]](using
    StreamEffect[S, F],
    StreamEffectToEffect[S]
) extends FarangoIntegrationSpec[F, S]:

  "A Database" - {
    "It should execute a simple query in database" in withCollection { collection =>
      for
        _      <- collection.insertDocument[StoredDocument](UserDocument(_key = "66", name = "Darth Vader"))
        result <- collection.database
                    .query[StoredDocument]("FOR s IN @@collection RETURN s", Map("@collection" -> "test-collection"))
                    .collect()
      yield result should contain only (UserDocument(_key = "66", "Darth Vader"))
    }
  }

object DatabaseIntegrationSpec:

  case class StoredDocument(_key: String, name: String)

  case class UserDocument(_key: String, name: String)

  given Transformer[UserDocument, StoredDocument] with

    override def apply[F[_]: Effect](a: UserDocument): F[StoredDocument] =
      Effect[F].attempt(StoredDocument(a._key, a.name))

  given Transformer[StoredDocument, UserDocument] with

    override def apply[F[_]: Effect](a: StoredDocument): F[UserDocument] =
      Effect[F].attempt(UserDocument(a._key, a.name))

class DatabaseIntegrationSpecWithTry extends DatabaseIntegrationSpec[Try, Vector]

class DatabaseIntegrationSpecWithEither extends DatabaseIntegrationSpec[[X] =>> Either[Throwable, X], Vector]

class DatabaseIntegrationSpecWithFuture extends DatabaseIntegrationSpec[[X] =>> Future[X], Vector]

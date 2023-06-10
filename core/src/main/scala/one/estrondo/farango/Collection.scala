package one.estrondo.farango

import com.arangodb.ArangoCollection
import com.arangodb.entity.IndexEntity
import com.arangodb.entity.InvertedIndexEntity
import com.arangodb.model.CollectionCreateOptions
import one.estrondo.farango.collection.PartiallyAppliedGetDocument
import scala.reflect.ClassTag

trait Collection:

  def arango: ArangoCollection

  def name: String

  def getDocument[S: ClassTag]: PartiallyAppliedGetDocument[S] = PartiallyAppliedGetDocument(arango)

object Collection:

  def apply[F[_]: Effect](
      database: Database,
      name: String,
      options: Option[CollectionCreateOptions] = None
  )(indexes: IndexEnsurer*): F[Collection] =
    Impl(database, name, options, indexes).create()
  private class Impl(
      database: Database,
      val name: String,
      options: Option[CollectionCreateOptions],
      indexes: Seq[IndexEnsurer]
  ) extends Collection:

    val arango = database.arango.collection(name)

    def create[F[_]: Effect](): F[Collection] =
      for
        _ <- Effect[F].attemptBlocking {
               options match
                 case Some(options) => arango.create(options)
                 case None          => arango.create()
             }
        _ <- createIndexes()
      yield this

    private def createIndexes[F[_]: Effect](): F[Seq[IndexEntity | InvertedIndexEntity]] =
      for result <- Effect[F].foreach(indexes) { index =>
                      index(arango)
                    }
      yield result

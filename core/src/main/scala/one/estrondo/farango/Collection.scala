package one.estrondo.farango

import com.arangodb.ArangoCollection
import com.arangodb.entity.IndexEntity
import com.arangodb.entity.InvertedIndexEntity
import com.arangodb.model.CollectionCreateOptions

trait Collection:

  def arango: ArangoCollection

object Collection:

  def apply[F[_]: Effect](
      database: Database,
      name: String,
      options: Option[CollectionCreateOptions] = None,
      indexes: Seq[IndexEnsurer] = Nil
  ): F[Collection] =
    Impl(database, name, options, indexes).create()
  private class Impl(
      database: Database,
      name: String,
      options: Option[CollectionCreateOptions] = None,
      indexes: Seq[IndexEnsurer] = Nil
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

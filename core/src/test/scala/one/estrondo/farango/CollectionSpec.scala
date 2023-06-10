package one.estrondo.farango

import com.arangodb.ArangoCollection
import com.arangodb.ArangoDatabase
import com.arangodb.entity.CollectionEntity
import com.arangodb.entity.IndexEntity
import com.arangodb.model.CollectionCreateOptions
import org.scalatest.matchers.HavePropertyMatcher
import scala.util.Try

abstract class CollectionSpec[F[_]: Effect: EffectToFuture] extends FarangoSpec[F]:

  "A Collection" - {

    "It should create a Arango's collection without options." in {
      val arango         = mock[ArangoDatabase]
      val database       = mock[Database]
      val mockCollection = mock[ArangoCollection]

      when(database.arango)
        .thenReturn(arango)

      when(arango.collection("test-collection"))
        .thenReturn(mockCollection)

      when(mockCollection.create())
        .thenReturn(CollectionEntity())

      for collection <- Collection(database, "test-collection")()
      yield collection should have(
        Symbol("name")("test-collection"),
        Symbol("arango")(mockCollection)
      )
    }

    "It should create a Arango's collection with options." in {
      val arango         = mock[ArangoDatabase]
      val database       = mock[Database]
      val mockCollection = mock[ArangoCollection]
      val options        = CollectionCreateOptions().numberOfShards(10)

      when(database.arango)
        .thenReturn(arango)

      when(arango.collection("test-collection"))
        .thenReturn(mockCollection)

      when(mockCollection.create(options))
        .thenReturn(CollectionEntity())

      for collection <- Collection(database, "test-collection", Some(options))()
      yield collection should have(
        Symbol("name")("test-collection"),
        Symbol("arango")(mockCollection)
      )
    }

    "It should a Arango's collection with indexes" in {
      val arango         = mock[ArangoDatabase]
      val database       = mock[Database]
      val mockCollection = mock[ArangoCollection]
      val indexEnsurer   = mock[IndexEnsurer]

      when(database.arango)
        .thenReturn(arango)

      when(arango.collection("test-collection"))
        .thenReturn(mockCollection)

      when(mockCollection.create())
        .thenReturn(CollectionEntity())

      when(indexEnsurer(mockCollection))
        .thenReturn(Effect[F].succeed(IndexEntity()))

      for collection <- Collection(database, "test-collection")(indexEnsurer)
      yield collection should have(
        Symbol("name")("test-collection"),
        Symbol("arango")(mockCollection)
      )
    }
  }

class CollectionSpecWithTry extends CollectionSpec[Try]

class CollectionSpecWithEither extends CollectionSpec[[X] =>> Either[Throwable, X]]

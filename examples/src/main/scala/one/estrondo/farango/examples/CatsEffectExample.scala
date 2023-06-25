package one.estrondo.farango.examples

import cats.effect.IO
import cats.effect.IOApp
import com.arangodb.model.DatabaseOptions
import com.arangodb.model.DBCreateOptions
import com.arangodb.model.DocumentCreateOptions
import com.arangodb.model.DocumentDeleteOptions
import com.arangodb.model.DocumentUpdateOptions
import com.arangodb.model.GeoIndexOptions
import com.arangodb.model.UserCreateOptions
import java.time.LocalDateTime
import one.estrondo.farango.Config
import one.estrondo.farango.DB
import one.estrondo.farango.IndexDescription
import one.estrondo.farango.cats.effect.given
import one.estrondo.farango.ducktape.given
import one.estrondo.farango.sync.SyncDB
import scala.jdk.CollectionConverters.MapHasAsJava

object CatsEffectExample extends IOApp.Simple:

  // podman run --rm -p 8529:8529 -e ARANGO_ROOT_PASSWORD=farango docker.io/library/arangodb:3.11.0

  override def run =
    val config = Config()
      .withUser("user")
      .withPassword("password")
      .withRootPassword("farango")
      .addHost("localhost", 8529)

    for
      db          <- IO.fromTry(SyncDB(config))
      username    <- createUser(db)
      defaultUser <- createDefaultUser(db)
      database    <-
        db.database(DBCreateOptions().name("zio-database").options(DatabaseOptions().sharding("sharding"))).create()
      collection  <- database
                       .collection("collection", Seq(IndexDescription.Geo(Seq("geom"), GeoIndexOptions().geoJson(true))))
                       .create()

      postIt = PostIt("My Post-it")

      createEntity <- collection
                        .insertDocument[StoredPostIt, CreatedPostIt](
                          postIt,
                          DocumentCreateOptions()
                            .returnOld(true)
                            .returnNew(true)
                        )
      _             = assert(createEntity.getOld == null)
      _             = assert(createEntity.getNew == CreatedPostIt(postIt.id))
      generatedKey  = createEntity.getKey

      applePostIt        = ApplePostIt("My Post-it too.")
      appleCreateEntity <- collection.insertDocument[StoredPostIt, CreatedPostIt](applePostIt)

      getPostIt <- collection.getDocument[StoredPostIt, PostIt](generatedKey)
      _          = assert(getPostIt == Some(postIt))

      result <- database
                  .query[StoredPostIt, PostIt](
                    "FOR postIt IN @@collection FILTER postIt.id == @id RETURN postIt",
                    Map(
                      "@collection" -> "collection",
                      "id"          -> postIt.id
                    )
                  )
                  .compile
                  .toList
      _       = assert(result == List(postIt))

      newLastUpdate = LocalDateTime.now()
      updateEntity <- collection.updateDocument[StoredPostIt, UpdateContent, UpdatedPostIt](
                        generatedKey,
                        postIt.copy(content = "New Content", lastUpdate = newLastUpdate),
                        DocumentUpdateOptions()
                          .returnOld(true)
                          .returnNew(true)
                      )

      _ = assert(updateEntity.getOld == UpdatedPostIt(postIt.id, "My Post-it"))
      _ = assert(updateEntity.getNew == UpdatedPostIt(postIt.id, "New Content"))

      deleteEntity <- collection.deleteDocument[StoredPostIt, DeletedPostIt](
                        generatedKey,
                        DocumentDeleteOptions().returnOld(true)
                      )
      _             = assert(deleteEntity.getOld == DeletedPostIt(postIt.id, "New Content", newLastUpdate))
    yield ()

  private def createUser(db: DB): IO[String] =
    for entity <-
        db.createUser("username", "password", UserCreateOptions().extra(Map("any-property-you-want" -> 1.0).asJava))
    yield entity.getUser

  private def createDefaultUser(db: DB): IO[String] =
    db.createDefaultUser().map(_.getUser)

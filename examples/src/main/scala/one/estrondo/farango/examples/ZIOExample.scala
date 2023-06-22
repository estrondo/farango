package one.estrondo.farango.examples

import com.arangodb.model.DatabaseOptions
import com.arangodb.model.DBCreateOptions
import com.arangodb.model.DocumentCreateOptions
import com.arangodb.model.DocumentUpdateOptions
import com.arangodb.model.GeoIndexOptions
import com.arangodb.model.UserCreateOptions
import java.time.LocalDateTime
import one.estrondo.farango.Config
import one.estrondo.farango.DB
import one.estrondo.farango.IndexDescription
import one.estrondo.farango.sync.SyncDB
import one.estrondo.farango.zio.given
import scala.jdk.CollectionConverters.MapHasAsJava
import zio.Scope
import zio.Task
import zio.ZIO
import zio.ZIOAppArgs
import zio.ZIOAppDefault

object ZIOExample extends ZIOAppDefault:

  // podman run --rm -p 8529:8529 -e ARANGO_ROOT_PASSWORD=farango docker.io/library/arangodb:3.11.0

  override def run: ZIO[ZIOAppArgs & Scope, Any, Any] =
    val config = Config()
      .withUser("user")
      .withPassword("password")
      .withRootPassword("farango")
      .addHost("localhost", 8529)

    for
      db          <- ZIO.fromTry(SyncDB(config))
      username    <- createUser(db)
      defaultUser <- createDefaultUser(db)

      database = db.database(DBCreateOptions().name("zio-database").options(DatabaseOptions().sharding("sharding")))
      _       <- database.create()

      collection =
        database.collection("collection", Seq(IndexDescription.Geo(Seq("geom"), GeoIndexOptions().geoJson(true))))
      _         <- collection.create()

      todo = Todo(
               id = "A",
               title = "A TODO Item.",
               date = LocalDateTime.now().plusDays(1),
               createdAt = LocalDateTime.now(),
               lastUpdate = None
             )

      createEntity <- collection.insertDocument[StoredTodo, TodoCreated](
                        todo,
                        DocumentCreateOptions().returnOld(true).returnNew(true)
                      )
      optTodoItem  <- collection.getDocument[StoredTodo, Todo](todo.id)

      result <- database
                  .query[StoredTodo, Todo](
                    "FOR todo IN @@collection FILTER todo._key == @key RETURN todo",
                    Map(
                      "@collection" -> "collection",
                      "key"         -> todo.id
                    )
                  )
                  .runCollect

      updateEntity <-
        collection.updateDocument[StoredTodo, UpdateTodoTitle, TodoUpdated](
          todo.id,
          todo.copy(title = "woohoo!"),
          DocumentUpdateOptions().returnOld(true).returnNew(true)
        )

      deleteEntity <- collection.deleteDocument[StoredTodo, TodoDeleted](todo.id)
    yield ()

  private def createUser(db: DB): Task[String] =
    for entity <-
        db.createUser("username", "password", UserCreateOptions().extra(Map("any-property-you-want" -> 1.0).asJava))
    yield entity.getUser

  private def createDefaultUser(db: DB): Task[String] =
    db.createDefaultUser().map(_.getUser)

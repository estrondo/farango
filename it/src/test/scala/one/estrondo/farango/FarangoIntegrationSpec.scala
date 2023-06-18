package one.estrondo.farango

import com.arangodb.entity.UserEntity
import com.dimafeng.testcontainers.GenericContainer
import com.dimafeng.testcontainers.scalatest.TestContainerForEach
import one.estrondo.farango.EffectOps.flatMap
import one.estrondo.farango.EffectOps.map
import one.estrondo.farango.sync.SyncDB
import org.scalatest.Assertion
import org.testcontainers.containers.wait.strategy.Wait
import scala.concurrent.Future
import scala.language.implicitConversions

abstract class FarangoIntegrationSpec[F[+_]: Effect: EffectToFuture, S[_]](using
    EffectStream[S, F],
    EffectStreamCollector[S, F]
) extends FarangoSpec[F, S],
      TestContainerForEach,
      TestDB:

  export EffectOps.flatMap
  export EffectOps.map

  override val containerDef: GenericContainer.Def[GenericContainer] = GenericContainer.Def(
    "docker.io/rthoth/estrondo:arangodb_test_3.11.0",
    exposedPorts = Seq(8529),
    waitStrategy = Wait.forLogMessage(""".*Have fun.*""", 1)
  )

  protected def withCollection(create: Boolean = true)(test: Collection => F[Assertion]): Future[Assertion] =
    withDatabase() { database =>
      val collection = database.collection("test-collection")

      if !create then test(collection)
      else
        for
          _         <- collection.create()
          assertion <- test(collection)
        yield assertion
    }

  protected def withDatabase(create: Boolean = true)(test: Database => F[Assertion]): Future[Assertion] =
    withDB() { db =>
      val database = db.database("test-database")

      if !create then test(database)
      else
        for
          _         <- database.create()
          assertion <- test(database)
        yield assertion
    }

  protected def withDB(createUser: Boolean = true)(
      test: DB => F[Assertion]
  ): Future[Assertion] =
    withContainers { container =>
      val config = Config()
        .withUser("test-user")
        .withPassword("test-password")
        .withRootPassword("farango")
        .addHost(container.host, container.mappedPort(8529))

      for
        db        <- createDB(config)
        _         <- if createUser then db.createUser("test-user", "test-password") else Effect[F].unit
        assertion <- test(db)
      yield assertion
    }

trait TestDB:

  def createDB[F[_]: Effect](config: Config): F[DB]

trait TestSyncDB extends TestDB:

  override def createDB[F[_]: Effect](config: Config): F[DB] =
    Effect[F].fromTry(SyncDB(config))

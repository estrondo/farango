package one.estrondo.farango

import com.arangodb.ArangoDB
import com.arangodb.model.CollectionCreateOptions
import com.dimafeng.testcontainers.GenericContainer
import com.dimafeng.testcontainers.scalatest.TestContainerForEach
import one.estrondo.farango.EffectOps.flatMap
import one.estrondo.farango.EffectOps.map
import org.scalatest.Assertion
import org.testcontainers.containers.wait.strategy.Wait
import scala.concurrent.Future

abstract class FarangoIntegrationSpec[F[_]: Effect: EffectToFuture, S[_]](using StreamEffectToEffect[S])
    extends FarangoSpec[F, S],
      TestContainerForEach:

  override val containerDef: GenericContainer.Def[GenericContainer] = GenericContainer.Def(
    "docker.io/rthoth/estrondo:arangodb_test_3.11.0",
    exposedPorts = Seq(8529),
    waitStrategy = Wait.forLogMessage(""".*Have fun.*""", 1)
  )

  protected def withCollection(block: Collection => F[Assertion]): Future[Assertion] =
    withDB { db =>
      for
        database   <- DB(db).db("test-database", true)
        collection <- database.collection("test-collection", Some(CollectionCreateOptions().waitForSync(true)))
        assertion  <- block(collection)
      yield assertion
    }

  protected def withDB(block: ArangoDB => Future[Assertion]): Future[Assertion] =
    withContainers { container =>
      block(
        builder()
          .host(container.host, container.mappedPort(8529))
          .user("root")
          .password("farango")
          .build()
      )
    }

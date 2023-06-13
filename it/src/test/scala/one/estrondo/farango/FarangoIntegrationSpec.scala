package one.estrondo.farango

import com.arangodb.ArangoDB
import com.dimafeng.testcontainers.GenericContainer
import com.dimafeng.testcontainers.scalatest.TestContainerForEach
import org.scalatest.Assertion
import org.testcontainers.containers.wait.strategy.Wait
import scala.concurrent.Future

abstract class FarangoIntegrationSpec[F[_]: Effect: EffectToFuture] extends FarangoSpec[F], TestContainerForEach:

  override val containerDef: GenericContainer.Def[GenericContainer] = GenericContainer.Def(
    "docker.io/rthoth/estrondo:arangodb_test_3.11.0",
    exposedPorts = Seq(8529),
    waitStrategy = Wait.forLogMessage(""".*Have fun.*""", 1)
  )

  def withDB(block: ArangoDB => Future[Assertion]): Future[Assertion] =
    withContainers { container =>
      block(
        builder()
          .host(container.host, container.mappedPort(8529))
          .user("root")
          .password("farango")
          .build()
      )
    }

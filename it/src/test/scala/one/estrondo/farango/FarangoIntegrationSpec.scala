package one.estrondo.farango

import com.arangodb.ArangoDB
import com.arangodb.ContentType
import com.arangodb.serde.jackson.JacksonSerde
import com.dimafeng.testcontainers.GenericContainer
import com.dimafeng.testcontainers.scalatest.TestContainerForEach
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import org.scalatest.Assertion
import org.testcontainers.containers.wait.strategy.Wait
import scala.concurrent.Future

abstract class FarangoIntegrationSpec[F[_]: Effect: EffectToFuture] extends FarangoSpec[F], TestContainerForEach:

  override val containerDef: GenericContainer.Def[GenericContainer] = GenericContainer.Def(
    "docker.io/library/arangodb:3.9.11",
    exposedPorts = Seq(8529),
    env = Map(
      "ARANGO_ROOT_PASSWORD" -> "farango"
    ),
    waitStrategy = Wait.forLogMessage(""".*Have fun.*""", 1)
  )

  def withDB(block: ArangoDB => Future[Assertion]): Future[Assertion] =
    withContainers { container =>
      val db = ArangoDB
        .Builder()
        .host(container.host, container.mappedPort(8529))
        .user("root")
        .password("farango")
        .serde(JacksonSerde.of(ContentType.JSON).configure(mapper => mapper.registerModule(DefaultScalaModule)))
        .build()

      block(db)
    }

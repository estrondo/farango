package one.estrondo.farango

import com.arangodb.ArangoDB
import com.arangodb.ContentType
import com.arangodb.Protocol
import com.arangodb.config.HostDescription
import com.arangodb.entity.LoadBalancingStrategy
import com.arangodb.serde.ArangoSerde
import com.arangodb.serde.jackson.JacksonSerde
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import javax.net.ssl.SSLContext
import scala.util.Try

case class ArangoBuilder(
    hosts: Seq[HostDescription] = Vector.empty,
    protocol: Option[Protocol] = None,
    timeout: Option[Integer] = None,
    user: Option[String] = None,
    password: Option[String] = None,
    jwt: Option[String] = None,
    useSsl: Option[Boolean] = None,
    sslContext: Option[SSLContext] = None,
    verifyHost: Option[Boolean] = None,
    chunkSize: Option[Integer] = None,
    maxConnections: Option[Integer] = None,
    connectionTtl: Option[Long] = None,
    keepAliveInterval: Option[Integer] = None,
    acquireHostList: Option[Boolean] = None,
    acquireHostListInterval: Option[Integer] = None,
    loadBalancingStrategy: Option[LoadBalancingStrategy] = None,
    serde: Option[ArangoSerde | JacksonConsumer] = None,
    responseQueueTimeSamples: Option[Integer] = None,
    rootPassword: Option[String] = None
):

  def addHost(host: String, port: Int): ArangoBuilder = copy(hosts :+ HostDescription(host, port))

  def withProtocol(value: Protocol): ArangoBuilder =
    copy(protocol = Option(value))

  def withTimeout(value: Integer): ArangoBuilder =
    copy(timeout = Option(value))

  def withUser(value: String): ArangoBuilder =
    copy(user = Option(value))

  def withPassword(value: String): ArangoBuilder =
    copy(password = Option(value))

  def withJwt(value: String): ArangoBuilder =
    copy(jwt = Option(value))

  def withUseSsl(value: Boolean): ArangoBuilder =
    copy(useSsl = Option(value))

  def withSslContext(value: SSLContext): ArangoBuilder =
    copy(sslContext = Option(value))

  def withVerifyHost(value: Boolean): ArangoBuilder =
    copy(verifyHost = Option(value))

  def withChunkSize(value: Integer): ArangoBuilder =
    copy(chunkSize = Option(value))

  def withMaxConnections(value: Integer): ArangoBuilder =
    copy(maxConnections = Option(value))

  def withConnectionTtl(value: Long): ArangoBuilder =
    copy(connectionTtl = Option(value))

  def withKeepAliveInterval(value: Integer): ArangoBuilder =
    copy(keepAliveInterval = Option(value))

  def withAcquireHostList(value: Boolean): ArangoBuilder =
    copy(acquireHostList = Option(value))

  def withAcquireHostListInterval(value: Integer): ArangoBuilder =
    copy(acquireHostListInterval = Option(value))

  def withLoadBalancingStrategy(value: LoadBalancingStrategy): ArangoBuilder =
    copy(loadBalancingStrategy = Option(value))

  def withSerde(value: ArangoSerde | JacksonConsumer): ArangoBuilder =
    copy(serde = Option(value))

  def withResponseQueueTimeSamples(value: Integer): ArangoBuilder =
    copy(responseQueueTimeSamples = Option(value))

  def withRootPassword(value: String): ArangoBuilder =
    copy(rootPassword = Option(value))

  def build(): Try[ArangoDB] =
    Try(createBuilder().build())

  private def createBuilder(): ArangoDB.Builder =
    val builder = ArangoDB.Builder()
    for description <- hosts do builder.host(description.getHost, description.getPort)
    if protocol.isDefined then builder.protocol(protocol.get)
    if timeout.isDefined then builder.timeout(timeout.get)
    if user.isDefined then builder.user(user.get)
    if password.isDefined then builder.password(password.get)
    if jwt.isDefined then builder.jwt(jwt.get)
    if useSsl.isDefined then builder.useSsl(useSsl.get)
    if sslContext.isDefined then builder.sslContext(sslContext.get)
    if verifyHost.isDefined then builder.verifyHost(verifyHost.get)
    if chunkSize.isDefined then builder.chunkSize(chunkSize.get)
    if maxConnections.isDefined then builder.maxConnections(maxConnections.get)
    if connectionTtl.isDefined then builder.connectionTtl(connectionTtl.get)
    if keepAliveInterval.isDefined then builder.keepAliveInterval(keepAliveInterval.get)
    if acquireHostList.isDefined then builder.acquireHostList(acquireHostList.get)
    if acquireHostListInterval.isDefined then builder.acquireHostListInterval(acquireHostListInterval.get)
    if loadBalancingStrategy.isDefined then builder.loadBalancingStrategy(loadBalancingStrategy.get)
    if responseQueueTimeSamples.isDefined then builder.responseQueueTimeSamples(responseQueueTimeSamples.get)

    def createDefaultSerde() = JacksonSerde.of(ContentType.JSON).configure { mapper =>
      mapper.registerModule(DefaultScalaModule)
    }

    val serde = this.serde match
      case Some(userSerde: ArangoSerde)    => userSerde
      case Some(consumer: JacksonConsumer) => createDefaultSerde().configure(consumer.apply)
      case None                            => createDefaultSerde()

    builder.serde(serde)

    builder

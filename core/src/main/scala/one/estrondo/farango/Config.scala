package one.estrondo.farango

import com.arangodb.ArangoDB
import com.arangodb.ArangoDBException
import com.arangodb.ContentType
import com.arangodb.Protocol
import com.arangodb.config.HostDescription
import com.arangodb.entity.LoadBalancingStrategy
import com.arangodb.serde.ArangoSerde
import com.arangodb.serde.jackson.JacksonSerde
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import javax.net.ssl.SSLContext
import scala.util.Failure
import scala.util.Try

/** Configuration used by Farango to connect to a Database Server. If you need to create database objects such as the
  * database and collections you need to inform the rootPassword configuration.
  */
case class Config(
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

  def addHost(host: String, port: Int): Config = copy(hosts :+ HostDescription(host, port))

  def withProtocol(value: Protocol): Config =
    copy(protocol = Option(value))

  def withTimeout(value: Integer): Config =
    copy(timeout = Option(value))

  def withUser(value: String): Config =
    copy(user = Option(value))

  def withPassword(value: String): Config =
    copy(password = Option(value))

  def withJwt(value: String): Config =
    copy(jwt = Option(value))

  def withUseSsl(value: Boolean): Config =
    copy(useSsl = Option(value))

  def withSslContext(value: SSLContext): Config =
    copy(sslContext = Option(value))

  def withVerifyHost(value: Boolean): Config =
    copy(verifyHost = Option(value))

  def withChunkSize(value: Integer): Config =
    copy(chunkSize = Option(value))

  def withMaxConnections(value: Integer): Config =
    copy(maxConnections = Option(value))

  def withConnectionTtl(value: Long): Config =
    copy(connectionTtl = Option(value))

  def withKeepAliveInterval(value: Integer): Config =
    copy(keepAliveInterval = Option(value))

  def withAcquireHostList(value: Boolean): Config =
    copy(acquireHostList = Option(value))

  def withAcquireHostListInterval(value: Integer): Config =
    copy(acquireHostListInterval = Option(value))

  def withLoadBalancingStrategy(value: LoadBalancingStrategy): Config =
    copy(loadBalancingStrategy = Option(value))

  def withSerde(value: ArangoSerde | JacksonConsumer): Config =
    copy(serde = Option(value))

  def withResponseQueueTimeSamples(value: Integer): Config =
    copy(responseQueueTimeSamples = Option(value))

  def withRootPassword(value: String): Config =
    copy(rootPassword = Option(value))

trait ConfigBuilder[A]:

  def user(config: Config): Try[A]

  def root(config: Config): Try[A]

object ConfigBuilder:

  given ConfigBuilder[ArangoDB] with

    override def root(config: Config): Try[ArangoDB] =
      build(config, Some("root"), config.rootPassword)

    private def build(config: Config, user: Option[String], password: Option[String]): Try[ArangoDB] =
      (user, password) match
        case (Some(user), Some(password)) => build(config, user, password)
        case (Some(_), _)                 => Failure(ArangoDBException("Farango: No password was defined!"))
        case (None, _)                    => Failure(ArangoDBException("Farango: No user was defined!"))
        case _                            => Failure(ArangoDBException("Farango: No used and/or password were defined!"))

    private def build(config: Config, user: String, password: String): Try[ArangoDB] =
      val builder = ArangoDB
        .Builder()
        .user(user)
        .password(password)

      if config.protocol.isDefined then builder.protocol(config.protocol.get)
      if config.timeout.isDefined then builder.timeout(config.timeout.get)
      if config.jwt.isDefined then builder.jwt(config.jwt.get)
      if config.useSsl.isDefined then builder.useSsl(config.useSsl.get)
      if config.sslContext.isDefined then builder.sslContext(config.sslContext.get)
      if config.verifyHost.isDefined then builder.verifyHost(config.verifyHost.get)
      if config.chunkSize.isDefined then builder.chunkSize(config.chunkSize.get)
      if config.maxConnections.isDefined then builder.maxConnections(config.maxConnections.get)
      if config.connectionTtl.isDefined then builder.connectionTtl(config.connectionTtl.get)
      if config.keepAliveInterval.isDefined then builder.keepAliveInterval(config.keepAliveInterval.get)
      if config.acquireHostList.isDefined then builder.acquireHostList(config.acquireHostList.get)
      if config.loadBalancingStrategy.isDefined then builder.loadBalancingStrategy(config.loadBalancingStrategy.get)

      if config.acquireHostListInterval.isDefined then
        builder.acquireHostListInterval(config.acquireHostListInterval.get)

      if config.responseQueueTimeSamples.isDefined then
        builder.responseQueueTimeSamples(config.responseQueueTimeSamples.get)

      def defaultSerde: JacksonSerde =
        JacksonSerde.of(ContentType.JSON).configure { mapper =>
          mapper.registerModules(
            DefaultScalaModule,
            Jdk8Module(),
            ParameterNamesModule(),
            JavaTimeModule()
          )
        }

      val serde = config.serde match
        case Some(userSerde: ArangoSerde)    => userSerde
        case Some(consumer: JacksonConsumer) => defaultSerde.configure(consumer.apply)
        case None                            => defaultSerde

      for host <- config.hosts do builder.host(host.getHost, host.getPort)

      Try(builder.serde(serde).build())

    override def user(config: Config): Try[ArangoDB] =
      build(config, config.user, config.password)

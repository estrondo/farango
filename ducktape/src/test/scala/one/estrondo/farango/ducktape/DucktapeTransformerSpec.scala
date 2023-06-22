package one.estrondo.farango.ducktape

import io.github.arainko.ducktape.Field
import io.github.arainko.ducktape.Transformer
import java.time.LocalDateTime
import java.util.UUID
import one.estrondo.farango.Effect
import one.estrondo.farango.EffectStream
import one.estrondo.farango.EffectStreamCollector
import one.estrondo.farango.EffectToFuture
import one.estrondo.farango.FarangoSpec
import one.estrondo.farango.FarangoTransformer
import one.estrondo.farango.given
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.language.implicitConversions
import scala.util.Try

abstract class DucktapeTransformerSpec[F[+_]: Effect: EffectToFuture, S[_]](using
    EffectStream[S, F],
    EffectStreamCollector[S, F]
) extends FarangoSpec[F, S]:

  given FarangoTransformer[DomainObject, StorageObject] = DucktapeTransformer(
    Transformer.define.build(Field.renamed(_._key, _.id))
  )

  case class DomainObject(id: UUID, title: String, createdAt: LocalDateTime)

  case class StorageObject(_key: UUID, title: String, createdAt: String)

  given Transformer[LocalDateTime, String] with
    override def transform(from: LocalDateTime): String = from.toString

  given Conversion[UUID, String] with
    override def apply(x: UUID): String = x.toString

  "A DucktapeTransformer instance" - {
    "It should convert a domain object to a storage model." in {
      val source = DomainObject(
        id = UUID.randomUUID(),
        title = s"title-${UUID.randomUUID()}",
        createdAt = LocalDateTime.now()
      )

      val expected = StorageObject(
        _key = source.id,
        title = source.title,
        createdAt = source.createdAt.toString
      )

      for transformed <- FarangoTransformer[DomainObject, StorageObject](source)
      yield transformed should be(expected)
    }
  }

class DucktapeTransformerSpecWithTry extends DucktapeTransformerSpec[Try, Vector]

class DucktapeTransformerSpecWithEither extends DucktapeTransformerSpec[[X] =>> Either[Throwable, X], List]

class DucktapeTransformerSpecWithFuture extends DucktapeTransformerSpec[Future, List]

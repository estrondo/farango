package one.estrondo.farango.test.domain

import java.time.Clock
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.UUID
import one.estrondo.farango.Transformer
import one.estrondo.farango.test.stored.StoredDocument
import scala.util.Random

case class DomainDocument(
    _key: String,
    title: String,
    length: Long,
    createdAt: LocalDateTime
)

object DomainDocumentFixture:

  def createNew(): DomainDocument =
    val key = UUID.randomUUID().toString
    DomainDocument(
      _key = key,
      title = s"title $key",
      length = Random.nextLong(100000000L),
      createdAt = LocalDateTime
        .now(Clock.systemUTC())
        .plusMinutes(Random.nextLong(2000) - 4000L)
        .truncatedTo(ChronoUnit.SECONDS)
    )

given Transformer[DomainDocument, StoredDocument] with

  override def transform(value: DomainDocument): StoredDocument = StoredDocument(
    _key = value._key,
    title = value.title,
    length = value.length,
    createdAt = value.createdAt,
    lastAccess = LocalDateTime.now(Clock.systemUTC()).truncatedTo(ChronoUnit.SECONDS)
  )

package one.estrondo.farango.test.stored

import java.time.LocalDateTime
import one.estrondo.farango.Transformer
import one.estrondo.farango.test.domain.DomainDocument

case class StoredDocument(
    _key: String,
    title: String,
    length: Long,
    createdAt: LocalDateTime,
    lastAccess: LocalDateTime
)

given Transformer[StoredDocument, DomainDocument] with

  override def transform(value: StoredDocument): DomainDocument =
    DomainDocument(
      _key = value._key,
      title = value.title,
      length = value.length,
      createdAt = value.createdAt
    )

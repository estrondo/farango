package one.estrondo.farango.test.stored

import java.time.LocalDateTime
import one.estrondo.farango.FarangoTransformer
import one.estrondo.farango.test.domain.DomainDocument
import one.estrondo.farango.test.domain.KeyDocument

case class StoredDocument(
    _key: String,
    title: String,
    length: Long,
    createdAt: LocalDateTime,
    lastAccess: LocalDateTime
)

given FarangoTransformer[StoredDocument, DomainDocument] with

  override def transform(value: StoredDocument): DomainDocument =
    DomainDocument(
      _key = value._key,
      title = value.title,
      length = value.length,
      createdAt = value.createdAt
    )

given FarangoTransformer[StoredDocument, KeyDocument] with

  override def transform(value: StoredDocument): KeyDocument = KeyDocument(value._key)

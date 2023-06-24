package one.estrondo.farango.examples

import io.github.arainko.ducktape.Field
import java.time.LocalDateTime
import java.util.UUID
import one.estrondo.farango.FarangoTransformer
import one.estrondo.farango.ducktape.DucktapeTransformer

case class PostIt(id: String, content: String, lastUpdate: LocalDateTime)

case class StoredPostIt(id: String, content: String, lastUpdate: LocalDateTime)

case class CreatedPostIt(id: String, createdAt: LocalDateTime)

case class UpdateContent(content: String, lastUpdate: LocalDateTime)

case class UpdatedPostIt(id: String, updatedAt: LocalDateTime)

case class DeletedPostIt(id: String, content: String, lastUpdate: LocalDateTime)

given FarangoTransformer[StoredPostIt, UpdatedPostIt] = DucktapeTransformer(
  Field.renamed(_.updatedAt, _.lastUpdate)
)

given FarangoTransformer[StoredPostIt, CreatedPostIt] = DucktapeTransformer(
  Field.renamed(_.createdAt, _.lastUpdate)
)

object PostIt:

  def apply(content: String): PostIt = PostIt(
    id = UUID.randomUUID().toString,
    content = content,
    lastUpdate = LocalDateTime.now()
  )

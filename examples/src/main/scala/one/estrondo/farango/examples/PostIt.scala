package one.estrondo.farango.examples

import io.github.arainko.ducktape.Field
import java.time.LocalDateTime
import java.util.UUID
import one.estrondo.farango.FarangoTransformer
import one.estrondo.farango.ducktape.DucktapeTransformer

case class PostIt(id: String, content: String, lastUpdate: LocalDateTime)

case class ApplePostIt(ID: String, content: String, lastUpdate: LocalDateTime)

case class StoredPostIt(id: String, content: String, lastUpdate: LocalDateTime)

case class CreatedPostIt(id: String)

case class UpdateContent(content: String, lastUpdate: LocalDateTime)

case class UpdatedPostIt(id: String, content: String)

case class DeletedPostIt(id: String, content: String, lastUpdate: LocalDateTime)

given FarangoTransformer[ApplePostIt, StoredPostIt] = DucktapeTransformer[ApplePostIt, StoredPostIt](
  Field.renamed(_.id, _.ID)
)

object PostIt:

  def apply(content: String): PostIt = PostIt(
    id = UUID.randomUUID().toString,
    content = content,
    lastUpdate = LocalDateTime.now()
  )

object ApplePostIt:

  def apply(content: String): ApplePostIt = ApplePostIt(
    ID = UUID.randomUUID().toString,
    content = content,
    lastUpdate = LocalDateTime.now()
  )

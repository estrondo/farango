package one.estrondo.farango.examples

import java.time.LocalDateTime

case class StoredTodo(
    _key: String,
    title: String,
    date: LocalDateTime,
    createdAt: LocalDateTime,
    updates: Seq[LocalDateTime]
)

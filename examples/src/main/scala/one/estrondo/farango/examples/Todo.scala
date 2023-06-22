package one.estrondo.farango.examples

import java.time.LocalDateTime

case class Todo(
    id: String,
    title: String,
    date: LocalDateTime,
    createdAt: LocalDateTime,
    lastUpdate: Option[LocalDateTime]
)

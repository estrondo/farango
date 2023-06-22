package one.estrondo.farango.examples

import io.github.arainko.ducktape.Field
import io.github.arainko.ducktape.Transformer
import one.estrondo.farango.FarangoTransformer
import one.estrondo.farango.ducktape.DucktapeTransformer

given FarangoTransformer[Todo, StoredTodo] =
  DucktapeTransformer(
    Transformer.define.build(
      Field.renamed(_._key, _.id),
      Field.computed(_.updates, _.lastUpdate.toList)
    )
  )

given FarangoTransformer[StoredTodo, TodoCreated] = DucktapeTransformer(
  Transformer.define.build(
    Field.renamed(_.id, _._key)
  )
)

given FarangoTransformer[StoredTodo, Todo] = DucktapeTransformer(
  Transformer.define.build(
    Field.renamed(_.id, _._key),
    Field.computed(_.lastUpdate, _.updates.lastOption)
  )
)

given FarangoTransformer[Todo, UpdateTodoTitle] = DucktapeTransformer(
  Transformer.define.build()
)

given FarangoTransformer[StoredTodo, TodoUpdated] = DucktapeTransformer(
  Transformer.define.build(
    Field.renamed(_.id, _._key)
  )
)

given FarangoTransformer[StoredTodo, TodoDeleted] = DucktapeTransformer(
  Transformer.define.build(
    Field.renamed(_.id, _._key)
  )
)

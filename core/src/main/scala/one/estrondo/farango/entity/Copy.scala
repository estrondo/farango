package one.estrondo.farango.entity

import com.arangodb.model.DBCreateOptions

trait Copy[T]:

  def apply(value: T): T

object Copy:

  inline def apply[T: Copy](value: T): T = summon[Copy[T]](value)

  given Copy[DBCreateOptions] with

    override def apply(value: DBCreateOptions): DBCreateOptions =
      DBCreateOptions()
        .options(value.getOptions)
        .name(value.getName)
        .users(value.getUsers)

package one.estrondo.farango

import com.arangodb.model.DBCreateOptions

trait Copy[T]:

  def copy(value: T): T

object Copy:

  def apply[T: Copy](value: T): T =
    summon[Copy[T]].copy(value)

given Copy[DBCreateOptions] with

  override def copy(value: DBCreateOptions): DBCreateOptions =
    DBCreateOptions()
      .name(value.getName)
      .options(value.getOptions)
      .users(value.getUsers)

package farango

import com.arangodb.model.DocumentUpdateOptions
import com.arangodb.entity.DocumentUpdateEntity

sealed trait UpdateReturn:
  private[farango] def configure(options: DocumentUpdateOptions): DocumentUpdateOptions
  private[farango] def apply[T](entity: DocumentUpdateEntity[T]): Option[T]

object UpdateReturn:
  case object New extends UpdateReturn:
    def configure(options: DocumentUpdateOptions): DocumentUpdateOptions =
      options
        .returnOld(false)
        .returnNew(true)

    def apply[T](entity: DocumentUpdateEntity[T]): Option[T] =
      Option(entity.getNew())

  case object Old extends UpdateReturn:
    def configure(options: DocumentUpdateOptions): DocumentUpdateOptions =
      options
        .returnNew(false)
        .returnOld(true)

    def apply[T](entity: DocumentUpdateEntity[T]): Option[T] =
      Option(entity.getOld())

  case object None extends UpdateReturn:
    def configure(options: DocumentUpdateOptions): DocumentUpdateOptions =
      options
        .returnNew(false)
        .returnOld(false)

    def apply[T](entity: DocumentUpdateEntity[T]): Option[T] =
      scala.None

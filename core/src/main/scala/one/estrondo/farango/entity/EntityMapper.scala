package one.estrondo.farango.entity

import com.arangodb.entity.DocumentCreateEntity
import com.arangodb.entity.DocumentDeleteEntity
import com.arangodb.entity.DocumentEntity
import com.arangodb.entity.DocumentUpdateEntity
import java.lang.reflect.Field
import one.estrondo.farango.Effect
import one.estrondo.farango.EffectOps.flatMap
import one.estrondo.farango.EffectOps.map
import one.estrondo.farango.Transformer

trait EntityMapper[E[_] <: DocumentEntity]:

  def map[A, B, F[+_]: Effect](entity: E[A])(using Transformer[A, B], Null <:< B): F[E[B]]

object EntityMapper:

  // TODO: Yeah! I'm using reflection...

  private val keyField = getField("key")
  private val idField  = getField("id")
  private val revField = getField("rev")

  inline def apply[E[_] <: DocumentEntity: EntityMapper]: EntityMapper[E] = summon[EntityMapper[E]]

  protected def copy[E <: DocumentEntity](source: DocumentEntity, target: E): E =
    keyField.set(target, source.getKey)
    idField.set(target, source.getId)
    revField.set(target, source.getRev)
    target

  private def getField(name: String, clazz: Class[_] = classOf[DocumentEntity]): Field =
    val field = clazz.getDeclaredField(name)
    field.setAccessible(true)
    field

  given EntityMapper[DocumentCreateEntity] with

    override def map[A, B, F[+_]: Effect](
        entity: DocumentCreateEntity[A]
    )(using Transformer[A, B], Null <:< B): F[DocumentCreateEntity[B]] =
      for
        oldValue <- Transformer[A, B].fromOption(Option(entity.getOld))
        newValue <- Transformer[A, B].fromOption(Option(entity.getNew))
      yield
        val newEntity = copy(entity, DocumentCreateEntity[B])
        newEntity.setOld(oldValue.orNull)
        newEntity.setNew(newValue.orNull)
        newEntity

  given EntityMapper[DocumentDeleteEntity] with

    override def map[A, B, F[+_]: Effect](
        entity: DocumentDeleteEntity[A]
    )(using Transformer[A, B], Null <:< B): F[DocumentDeleteEntity[B]] =
      for oldValue <- Transformer[A, B].fromOption(Option(entity.getOld))
      yield
        val newEntity = copy(entity, DocumentDeleteEntity[B])
        newEntity.setOld(oldValue.orNull)
        newEntity

  given EntityMapper[DocumentUpdateEntity] with

    private val oldRefField = getField("oldRev", classOf[DocumentUpdateEntity[_]])
    override def map[A, B, F[+_]: Effect](
        entity: DocumentUpdateEntity[A]
    )(using Transformer[A, B], Null <:< B): F[DocumentUpdateEntity[B]] =
      for
        oldValue <- Transformer[A, B].fromOption(Option(entity.getOld))
        newValue <- Transformer[A, B].fromOption(Option(entity.getNew))
      yield
        val newEntity = copy(entity, DocumentUpdateEntity[B])
        oldRefField.set(newEntity, entity.getOldRev)
        newEntity.setOld(oldValue.orNull)
        newEntity.setNew(newValue.orNull)
        newEntity

package one.estrondo.farango.collection

import com.arangodb.entity.DocumentCreateEntity
import com.arangodb.entity.DocumentDeleteEntity
import com.arangodb.entity.DocumentUpdateEntity
import com.arangodb.model.DocumentCreateOptions
import com.arangodb.model.DocumentDeleteOptions
import com.arangodb.model.DocumentReadOptions
import com.arangodb.model.DocumentUpdateOptions
import one.estrondo.farango.Composed
import one.estrondo.farango.Effect
import one.estrondo.farango.EffectOps.flatMap
import one.estrondo.farango.EffectOps.map
import one.estrondo.farango.FarangoTransformer
import one.estrondo.farango.entity.EntityMapper
import one.estrondo.farango.typeOf
import scala.reflect.ClassTag

trait PartialDeleteDocument[A, R] extends Composed:

  def apply[F[+_]: Effect](using FarangoTransformer[A, R])(using
      EntityMapper[DocumentDeleteEntity],
      ClassTag[A],
      Null <:< R
  )(key: String, options: DocumentDeleteOptions = DocumentDeleteOptions()): F[DocumentDeleteEntity[R]] =
    for
      entity <- blockingCompose(remove(key, options, typeOf[A]))
      mapped <- EntityMapper[DocumentDeleteEntity].map(entity)
    yield mapped

  protected def remove(key: String, options: DocumentDeleteOptions, returnType: Class[A]): G[DocumentDeleteEntity[A]]

trait PartialGetDocument[A, R] extends Composed:

  def apply[F[+_]: Effect](using FarangoTransformer[A, R])(using
      ClassTag[A]
  )(key: String, options: DocumentReadOptions = DocumentReadOptions()): F[Option[R]] =
    for
      restored    <- blockingCompose(get(key, options, typeOf[A]))
      transformed <- FarangoTransformer[A, R].fromOption(restored)
    yield transformed

  protected def get(key: String, options: DocumentReadOptions, returnType: Class[A]): G[Option[A]]

trait PartialInsertDocument[A, R] extends Composed:

  def apply[T, F[+_]: Effect](document: T, options: DocumentCreateOptions = DocumentCreateOptions())(using
      FarangoTransformer[T, A],
      FarangoTransformer[A, R]
  )(using
      EntityMapper[DocumentCreateEntity],
      ClassTag[A],
      Null <:< R
  ): F[DocumentCreateEntity[R]] =
    for
      transformed  <- FarangoTransformer[T, A](document)
      entity       <- blockingCompose(insert(transformed, options, typeOf[A]))
      mappedEntity <- EntityMapper[DocumentCreateEntity].map(entity)
    yield mappedEntity

  protected def insert(document: A, options: DocumentCreateOptions, returnType: Class[A]): G[DocumentCreateEntity[A]]

trait PartialUpdateDocument[A, U, R] extends Composed:

  def apply[T, F[+_]: Effect](key: String, value: T, options: DocumentUpdateOptions = DocumentUpdateOptions())(using
      FarangoTransformer[T, U]
  )(using FarangoTransformer[A, R])(using
      EntityMapper[DocumentUpdateEntity],
      ClassTag[A],
      Null <:< R
  ): F[DocumentUpdateEntity[R]] =
    for
      transformedValue <- FarangoTransformer[T, U](value)
      entity           <- blockingCompose(update(key, transformedValue, options, typeOf[A]))
      mapped           <- EntityMapper[DocumentUpdateEntity].map(entity)
    yield mapped

  protected def update(
      key: String,
      value: U,
      options: DocumentUpdateOptions,
      returnType: Class[A]
  ): G[DocumentUpdateEntity[A]]

package one.estrondo.farango.collection

import com.arangodb.entity.DocumentCreateEntity
import com.arangodb.model.DocumentCreateOptions
import com.arangodb.model.DocumentReadOptions
import one.estrondo.farango.Composed
import one.estrondo.farango.Effect
import one.estrondo.farango.EffectOps.flatMap
import one.estrondo.farango.EffectOps.map
import one.estrondo.farango.Transformer
import scala.reflect.ClassTag

trait PartialGetDocument[A] extends Composed:

  def apply[T, F[+_]: Effect](using
      Transformer[A, T],
      ClassTag[A]
  )(key: String, options: DocumentReadOptions = DocumentReadOptions()): F[Option[T]] =
    for
      restored    <- compose(get(key, options))
      transformed <- Transformer[A, T].fromOption(restored)
    yield transformed

  protected def get(key: String, options: DocumentReadOptions)(using ClassTag[A]): G[Option[A]]

trait PartialInsertDocument[A] extends Composed:

  def apply[T, F[_]: Effect](document: T, options: DocumentCreateOptions = DocumentCreateOptions())(using
      Transformer[T, A],
      ClassTag[A]
  ): F[DocumentCreateEntity[A]] =
    for
      transformed <- Transformer[T, A](document)
      entity      <- compose(insert(transformed, options))
    yield entity

  protected def insert(document: A, options: DocumentCreateOptions)(using ClassTag[A]): G[DocumentCreateEntity[A]]

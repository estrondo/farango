package one.estrondo.farango

import java.util.stream
import java.util.stream.Collectors
import scala.collection.Factory
import scala.jdk.CollectionConverters.ListHasAsScala
import scala.util.Failure
import scala.util.Success
import scala.util.Try

//noinspection ScalaFileName
given Effect[Try] with

  override def attempt[A](value: => A): Try[A] =
    Try(value)

  override def attemptBlocking[A](value: => A): Try[A] =
    Try(value)

  override def fail[A](cause: => Throwable): Try[A] =
    Failure(cause)

  override def flatMap[A, B](a: Try[A])(f: A => Try[B]): Try[B] =
    a.flatMap(f)

  override def map[A, B](a: Try[A])(f: A => B): Try[B] =
    a.map(f)

  override def succeed[A](value: => A): Try[A] =
    Success(value)

given [I[_] <: Iterable[_]](using factory: Factory[Any, I[Any]]): StreamEffect[I, Try] with

  override def fromEffect[A](a: => Try[stream.Stream[A]]): I[A] =
    val builder = factory.newBuilder
    a match
      case Success(value) =>
        builder.addAll(value.collect(Collectors.toList).asScala)
        builder.result().asInstanceOf[I[A]]

      case Failure(exception) =>
        throw exception

  override def mapEffect[A, B](a: I[A])(f: A => Try[B]): I[B] =
    val builder = factory.newBuilder
    for va <- a do
      f(va.asInstanceOf[A]) match
        case Success(value) =>
          builder.addOne(value)

        case Failure(exception) =>
          throw exception

    builder.result().asInstanceOf[I[B]]

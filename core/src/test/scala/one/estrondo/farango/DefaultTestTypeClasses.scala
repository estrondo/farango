package one.estrondo.farango

import java.util.stream
import java.util.stream.Collector
import java.util.stream.Collectors
import scala.collection.Factory
import scala.concurrent.Await
import scala.concurrent.Future
import scala.concurrent.duration.Duration
import scala.jdk.CollectionConverters.ListHasAsScala
import scala.util.Try

//noinspection ScalaFileName
given EffectToFuture[Try] with

  override def apply[A](a: Try[A]): Future[A] = Future.fromTry(a)

given EffectToFuture[[X] =>> Either[Throwable, X]] with

  override def apply[A](a: Either[Throwable, A]): Future[A] = Future.fromTry(a.toTry)

given EffectToFuture[Future] with

  override def apply[A](a: Future[A]): Future[A] = a

given Extractor[Try] with

  override def get[A](a: Try[A]): A = a.get

given Extractor[[X] =>> Either[Throwable, X]] with

  override def get[A](a: Either[Throwable, A]): A = a.toTry.get

given Extractor[Future] with

  override def get[A](a: Future[A]): A = Await.result(a, Duration.Inf)

given [I[_] <: Iterable[_], F[_]: Extractor](using Factory[Any, I[Any]]): EffectStream[I, F] with

  override def fromJavaStream[A](a: F[stream.Stream[A]]): I[A] =
    val list    = summon[Extractor[F]].get(a).collect(Collectors.toList)
    val builder = summon[Factory[Any, I[Any]]].newBuilder
    for item <- list.asScala do builder.addOne(item.asInstanceOf[A])

    builder.result().asInstanceOf[I[A]]

  override def map[A, B](a: I[A])(f: A => F[B]): I[B] =
    val extractor = summon[Extractor[F]]
    a.map(x => extractor.get(f(x.asInstanceOf[A]))).asInstanceOf[I[B]]

given [I[_] <: Iterable[_], F[_]: Effect]: EffectStreamCollector[I, F] with

  override def apply[A](a: I[A]): F[Seq[A]] =
    Effect[F].fromTry(Try(a.toSeq.asInstanceOf[Seq[A]]))

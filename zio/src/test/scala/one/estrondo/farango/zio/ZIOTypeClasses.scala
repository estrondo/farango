package one.estrondo.farango.zio

import one.estrondo.farango.EffectStreamCollector
import one.estrondo.farango.EffectToFuture
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import zio.Runtime
import zio.Task
import zio.Unsafe
import zio.stream.ZStream

//noinspection ScalaFileName
given EffectToFuture[Task] with

  override def apply[A](a: Task[A]): Future[A] =
    Future {
      Unsafe.unsafe { implicit unsafe =>
        Runtime.default.unsafe.run(a).getOrThrow()
      }
    }

given EffectStreamCollector[[X] =>> ZStream[Any, Throwable, X], Task] with

  override def apply[A](a: ZStream[Any, Throwable, A]): Task[Seq[A]] =
    a.runCollect

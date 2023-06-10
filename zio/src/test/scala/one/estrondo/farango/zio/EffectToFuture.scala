package one.estrondo.farango.zio

import one.estrondo.farango.EffectToFuture
import org.scalatest.Assertion
import scala.concurrent.Future
import zio.*
import scala.concurrent.ExecutionContext.Implicits.global

//noinspection ScalaFileName
given EffectToFuture[[X] =>> Task[X]] with

  override def toFuture(a: Task[Assertion]): Future[Assertion] =
    Future {
      Unsafe.unsafe { implicit unsafe =>
        Runtime.default.unsafe.run(a).getOrThrow()
      }
    }

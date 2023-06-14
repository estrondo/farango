package one.estrondo.farango

import org.mockito.ArgumentMatchers
import org.mockito.Mockito
import org.mockito.stubbing.OngoingStubbing
import org.scalatest.Assertion
import org.scalatest.freespec.AsyncFreeSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.mockito.MockitoSugar
import scala.concurrent.Future
import scala.reflect.ClassTag

abstract class FarangoSpec[F[_]: Effect: EffectToFuture, S[_]](using StreamEffectToEffect[S])
    extends AsyncFreeSpec,
      Matchers:

  export ArgumentMatchers.{eq => eqTo}
  export Mockito.verify
  export MockitoSugar.mock

  protected inline def when[R](inline methodCall: R): OngoingStubbing[R] =
    Mockito.when(methodCall)

  protected inline def any[T: ClassTag]: T =
    ArgumentMatchers.any(summon[ClassTag[T]].runtimeClass.asInstanceOf[Class[T]])

  given toFuture: Conversion[F[Assertion], Future[Assertion]] with

    override def apply(x: F[Assertion]): Future[Assertion] =
      summon[EffectToFuture[F]].toFuture(x)

  extension [A](stream: S[A]) def collect(): F[Iterable[A]] = summon[StreamEffectToEffect[S]].collect(stream)

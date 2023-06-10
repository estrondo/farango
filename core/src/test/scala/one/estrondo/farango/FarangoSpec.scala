package one.estrondo.farango

import org.mockito.Mockito
import org.mockito.stubbing.OngoingStubbing
import org.scalatest.Assertion
import org.scalatest.freespec.AsyncFreeSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.mockito.MockitoSugar
import scala.concurrent.Future

abstract class FarangoSpec[F[_]: EffectToFuture] extends AsyncFreeSpec, Matchers:

  export Mockito.verify
  export MockitoSugar.mock

  protected inline def when[R](inline methodCall: R): OngoingStubbing[R] =
    Mockito.when(methodCall)

  given toFuture: Conversion[F[Assertion], Future[Assertion]] with

    override def apply(x: F[Assertion]): Future[Assertion] =
      summon[EffectToFuture[F]].toFuture(x)

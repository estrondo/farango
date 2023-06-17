package one.estrondo.farango

import org.mockito.ArgumentMatchers
import org.mockito.Mockito
import org.mockito.invocation.InvocationOnMock
import org.mockito.stubbing.Answer
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

  private var ongoingStubbing = false

  protected def mock[T <: AnyRef: ClassTag]: T =
    MockitoSugar.mock[T](defaultAnswer)

  protected def defaultAnswer: Answer[_] = (_: InvocationOnMock) =>
    if ongoingStubbing then null
    else throw new IllegalStateException("Invalid call!")

  protected def when[R](methodCall: => R): OngoingStubbing[R] =
    ongoingStubbing = true
    try
      Mockito.when(methodCall)
    finally
      ongoingStubbing = false

  protected inline def any[T: ClassTag]: T =
    ArgumentMatchers.any(summon[ClassTag[T]].runtimeClass.asInstanceOf[Class[T]])

  given toFuture: Conversion[F[Assertion], Future[Assertion]] with

    override def apply(x: F[Assertion]): Future[Assertion] =
      summon[EffectToFuture[F]].toFuture(x)

  extension [A](stream: S[A]) def collect(): F[Iterable[A]] = summon[StreamEffectToEffect[S]].collect(stream)

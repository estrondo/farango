package one.estrondo.farango

import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.eq
import org.mockito.Mockito
import org.mockito.invocation.InvocationOnMock
import org.mockito.stubbing.Answer
import org.mockito.stubbing.OngoingStubbing
import org.scalatestplus.mockito.MockitoSugar
import scala.reflect.ClassTag

trait MockitoOps:

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

package one.estrondo.farango

import org.scalatest.freespec.AsyncFreeSpec
import org.scalatest.matchers.should.Matchers
import scala.concurrent.Future

abstract class FarangoSpec[F[_]: Effect: EffectToFuture, S[_]](using EffectStream[S, F], EffectStreamCollector[S, F])
    extends AsyncFreeSpec,
      MockitoOps,
      Matchers:

  given [A]: Conversion[F[A], Future[A]] with
    override def apply(x: F[A]): Future[A] =
      summon[EffectToFuture[F]](x)

  extension [A](stream: S[A])
    def collect(): F[Seq[A]] =
      summon[EffectStreamCollector[S, F]](stream)

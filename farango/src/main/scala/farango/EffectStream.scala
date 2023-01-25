package farango

import com.arangodb.ArangoCursor

import java.util.concurrent.CompletionStage
import scala.util.Try

object EffectStream:

  transparent inline def apply[S[_], F[_]](using inline effect: EffectStream[S, F]): EffectStream[S, F] =
    effect

trait EffectStream[S[_], F[_]]:

  type Effect[_]

  type JavaStream[T] = java.util.stream.Stream[T]

  def effectMap[A, B](stream: S[A])(fn: A => F[B]): S[B]

  def mapFromJavaStream[A, B](stream: => JavaStream[A])(fn: A => B): S[B]

  def mapFromCompletionStage[A, B](stream: => CompletionStage[JavaStream[A]])(fn: A => B): S[B]

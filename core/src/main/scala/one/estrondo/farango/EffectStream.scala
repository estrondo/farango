package one.estrondo.farango

import java.util.stream

trait EffectStream[S[_], F[_]]:

  def fromJavaStream[A](a: F[stream.Stream[A]]): S[A]

  def map[A, B](a: S[A])(f: A => F[B]): S[B]

object EffectStream:

  inline def apply[S[_], F[_]](using EffectStream[S, F]): EffectStream[S, F] =
    summon[EffectStream[S, F]]

object EffectStreamOps:

  extension [A, S[_], F[_]](a: S[A])(using EffectStream[S, F])
    def map[B](f: A => F[B]): S[B] =
      EffectStream[S, F].map(a)(f)

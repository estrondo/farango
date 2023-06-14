package one.estrondo.farango

import java.util.stream.{Stream => JavaStream}

trait StreamEffect[S[_], F[_]]:

  def fromEffect[A](a: => F[JavaStream[A]]): S[A]

  def mapEffect[A, B](a: S[A])(f: A => F[B]): S[B]

object StreamEffect:

  inline def apply[S[_], F[_]](using inline streamEffect: StreamEffect[S, F]): StreamEffect[S, F] =
    streamEffect

object StreamEffectOps:
  extension [A, F[_], S[_]](a: S[A])(using StreamEffect[S, F], Effect[F])
    def flatMap[B](f: A => S[B]): S[B] = ???

    def map[B](f: A => B): S[B] = ???

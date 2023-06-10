package one.estrondo.farango

//noinspection ScalaFileName
given Effect[[X] =>> Either[Throwable, X]] with

  override def attemptBlocking[A](value: => A): Either[Throwable, A] =
    attempt(value)

  override def attempt[A](value: => A): Either[Throwable, A] =
    try Right(value)
    catch case error: Throwable => Left(error)

  override def fail[A](cause: => Throwable): Either[Throwable, A] =
    Left(cause)

  override def flatMap[A, B](a: Either[Throwable, A])(f: A => Either[Throwable, B]): Either[Throwable, B] =
    a.flatMap(f)

  override def succeed[A](value: => A): Either[Throwable, A] =
    Right(value)

  override def map[A, B](a: Either[Throwable, A])(f: A => B): Either[Throwable, B] =
    a.map(f)

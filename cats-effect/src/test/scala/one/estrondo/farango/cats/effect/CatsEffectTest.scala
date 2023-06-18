package one.estrondo.farango.cats.effect

import cats.effect.IO
import one.estrondo.farango.SyncDBSpec

class SyncDBSpecWithIO extends SyncDBSpec[IO, [O] =>> fs2.Stream[IO, O]]

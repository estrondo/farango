package one.estrondo.farango.cats.effect

import cats.effect.IO
import one.estrondo.farango.SyncCollectionIntegrationSpec
import one.estrondo.farango.SyncDBIntegrationSpec

class SyncDBIntegrationSpecWithIO extends SyncDBIntegrationSpec[IO, [O] =>> fs2.Stream[IO, O]]

class SyncCollectionIntegrationSpecWithIO extends SyncCollectionIntegrationSpec[IO, [O] =>> fs2.Stream[IO, O]]

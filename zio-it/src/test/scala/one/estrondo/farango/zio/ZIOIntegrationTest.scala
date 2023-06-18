package one.estrondo.farango.zio

import one.estrondo.farango.SyncCollectionIntegrationSpec
import one.estrondo.farango.SyncDBIntegrationSpec
import zio.Task
import zio.stream.ZStream

//noinspection ScalaFileName
class SyncDBIntegrationSpecWithZIO extends SyncDBIntegrationSpec[Task, [A] =>> ZStream[Any, Throwable, A]]

class SyncCollectionIntegrationSpecWithZIO
    extends SyncCollectionIntegrationSpec[Task, [A] =>> ZStream[Any, Throwable, A]]

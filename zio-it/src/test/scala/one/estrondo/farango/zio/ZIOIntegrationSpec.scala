package one.estrondo.farango.zio

import one.estrondo.farango.CollectionIntegrationSpec
import one.estrondo.farango.DatabaseIntegrationSpec
import one.estrondo.farango.DBIntegrationSpec
import zio.Task
import zio.stream.ZStream

class DBIntegrationSpecWithZIO extends DBIntegrationSpec[[X] =>> Task[X], [Y] =>> ZStream[Any, Throwable, Y]]

class CollectionIntegrationSpecWithZIO
    extends CollectionIntegrationSpec[[X] =>> Task[X], [Y] =>> ZStream[Any, Throwable, Y]]

class DatabaseIntegrationSpecWithZIO
    extends DatabaseIntegrationSpec[[X] =>> Task[X], [Y] =>> ZStream[Any, Throwable, Y]]

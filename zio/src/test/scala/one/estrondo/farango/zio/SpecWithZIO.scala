package one.estrondo.farango.zio

import one.estrondo.farango.CollectionSpec
import one.estrondo.farango.DatabaseSpec
import one.estrondo.farango.DBSpec
import zio.Task
import zio.stream.ZStream

class DBSpecWithZIO extends DBSpec[[X] =>> Task[X], [Y] =>> ZStream[Any, Throwable, Y]]

class CollectionSpecWithZIO extends CollectionSpec[[X] =>> Task[X], [Y] =>> ZStream[Any, Throwable, Y]]

class DatabaseSpecWithZIO extends DatabaseSpec[[X] =>> Task[X], [Y] =>> ZStream[Any, Throwable, Y]]

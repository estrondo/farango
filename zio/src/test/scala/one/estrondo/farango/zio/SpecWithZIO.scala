package one.estrondo.farango.zio

import one.estrondo.farango.CollectionSpec
import one.estrondo.farango.DatabaseSpec
import zio.Task
import zio.stream.ZStream

class CollectionSpecWithZIO extends CollectionSpec[[X] =>> Task[X], [Y] =>> ZStream[Any, Throwable, Y]]

class DatabaseSpecWithZIO extends DatabaseSpec[[X] =>> Task[X], [Y] =>> ZStream[Any, Throwable, Y]]

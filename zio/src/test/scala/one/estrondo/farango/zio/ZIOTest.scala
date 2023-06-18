package one.estrondo.farango.zio

import one.estrondo.farango.SyncDBSpec
import zio.Task
import zio.stream.ZStream

class SyncDBSpecWithZIO extends SyncDBSpec[Task, [X] =>> ZStream[Any, Throwable, X]]

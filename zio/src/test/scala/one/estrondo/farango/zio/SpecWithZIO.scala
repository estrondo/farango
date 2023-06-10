package one.estrondo.farango.zio

import one.estrondo.farango.CollectionSpec
import one.estrondo.farango.DBSpec
import zio.Task

class DBSpecWithZIO extends DBSpec[[X] =>> Task[X]]

class CollectionSpecWithZIO extends CollectionSpec[[X] =>> Task[X]]

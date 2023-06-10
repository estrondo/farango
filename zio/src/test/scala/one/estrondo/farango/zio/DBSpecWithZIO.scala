package one.estrondo.farango.zio

import one.estrondo.farango.DBSpec
import zio.Task

class DBSpecWithZIO extends DBSpec[[X] =>> Task[X]]

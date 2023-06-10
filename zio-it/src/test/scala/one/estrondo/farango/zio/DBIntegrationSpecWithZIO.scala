package one.estrondo.farango.zio

import one.estrondo.farango.DBIntegrationSpec
import zio.Task

class DBIntegrationSpecWithZIO extends DBIntegrationSpec[[X] =>> Task[X]]

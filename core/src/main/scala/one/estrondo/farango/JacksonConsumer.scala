package one.estrondo.farango

import com.fasterxml.jackson.databind.ObjectMapper

class JacksonConsumer(consumer: ObjectMapper => Unit):

  def apply(mapper: ObjectMapper): Unit = consumer(mapper)

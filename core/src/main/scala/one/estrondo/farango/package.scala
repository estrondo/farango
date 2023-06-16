package one.estrondo.farango

import com.arangodb.ArangoDB
import com.arangodb.ContentType
import com.arangodb.serde.jackson.JacksonSerde
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule

def builder(): ArangoDB.Builder =
  ArangoDB
    .Builder()
    .serde(
      JacksonSerde
        .of(ContentType.JSON)
        .configure(mapper => mapper.registerModule(DefaultScalaModule))
    )

def builder(objectMapperFn: ObjectMapper => Unit): ArangoDB.Builder =
  ArangoDB
    .Builder()
    .serde(
      JacksonSerde
        .of(ContentType.JSON)
        .configure(mapper => {
          mapper.registerModule(DefaultScalaModule)
          objectMapperFn(mapper)
        })
    )

package one.estrondo.farango

import com.arangodb.ArangoDB
import com.arangodb.model.DBCreateOptions

trait DB:

  def arango: ArangoDB
  def db(nameOrOptions: String | DBCreateOptions): Database

object DB:

  def apply(arangoDB: ArangoDB): DB =
    new Impl(arangoDB)

  private class Impl(val arango: ArangoDB) extends DB:

    override def db(nameOrOptions: String | DBCreateOptions): Database =
      Database(this, nameOrOptions)

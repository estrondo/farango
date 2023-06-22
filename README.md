# Farango

A small Scala 3 wrapper for [ArangoDB](http://www.arangodb.com).

## Why?

So, I have been working on a project that utilises ArangoDB and [ZIO](http://www.zio.dev). I would like to use Arango due to its geographical index support. I'm enjoyng coding in functional fashion. Therefore, after some mess and some ideas I have decide to move all codes to an external library and publish it. So, it's the very beginning of this project and I have just done the basic set of features and functionalities to help me in my project. Any help such as ideas, features and fixes are welcome.


## Functional Programming

Farango(_Functional Arango_) has built in support for [ZIO](http://www.zio.dev), [Cats Effect](https://typelevel.org/cats-effect), Scala's Future, Try and Either[Throwable, _] types.


## Overview

### Starting

Add in your `build.sbt` one of the following dependencies:

```scala

"one.estrondo.farango" %% "farango" % "0.1.0" // If you want to use just the Scala's types.

"one.estrondo.farango" %% "farango-zio" % "0.1.0" // If you want working with ZIO.

"one.estrondo.farango" %% "farango-cats-effect" % "0.1.1" // If you want Cats Effect.

```

Farango was written with functional programming style kept in mind. Therefore, it uses the fammous Effectfull type `F[_]`. In order to use Farango you have to make some imports in your code.

```scala

import one.estrondo.farango.*

import one.estrondo.farango.zio.given // If you are using ZIO.

import one.estrondo.farango.cats.effect.given // it you are using Cats Effect.

```

### Creating a DB instance.

Farango strives to resemble the ArangoDB's Java Drive design, but to have the ability of create/recreate a database as many times as you want Farango needs to keep all configuration information, because of this you have to create a Farango Config object (`one.estrondo.farango.Config`).

```scala

val config = Config()
  .addHost("localhost", 8529)
  .withUser("user")
  .withPassword("user password")
  .withRootPassword("Arango's root password") // You need this in order to create databases and collections through Farango.

```

After that it's time to have some fun, or at least attempt to do so. To create a Farango's DB version of Arango's DB you currently need to utilise the object `one.estrondo.farango.SyncDB`. Prior to version 7.x of ArangoDB Java Driver had `async` and `sync` clients. However, in the current version of ArangoDB Java Driver only supports a `sync` client, in their website they say the support to `async` will be re-added in a future version 7.x. Hence, Farango provides a `SyncDB` object which acts as a Factory for the `one.estrondo.farango.DB`.

```scala

import one.estrondo.farango.sync.SyncDB

val db = SyncDB(config) // SyncDB will return a Try[DB].

```

If you are using ZIO you would do like this:

```scala

val db = ZIO.fromTry(SyncDB(config))

```

If you are using Cats Effect you would do like this:

```scala

val db = IO.fromTry(SyncDB(config))

```

Or you are using `scala.concurrent.Future`:

```scala

val db = Future.fromTry(SyncDB(config))

```

You are free to choice, or you could use the `scala.util.Try` directly or map it to a `scala.util.Either` object.

You had made your choice of which framework to use to build your application, let's see some examples.


### Creating a new user.

Let's remember one small thing, in ArangoDB Java Driver an DB instance represents not a specific database, but it represents the database server. Now that we have our DB object, let's assume that we want to create a user on the database server. This can be accomplished with `createUser(user, password, options)` method. The parameter `options` is a [`UserCreateOptions`](https://www.javadoc.io/doc/com.arangodb/arangodb-java-driver/latest/com/arangodb/model/UserCreateOptions.html) and the result type is [`UserEntity`](https://www.javadoc.io/doc/com.arangodb/arangodb-java-driver/latest/com/arangodb/entity/UserEntity.html) both from ArangoDB Java Driver. Actually, the result type is a `F[UserEntity]`.


```scala

for
  entity <- db.createUser("user-login", "user-password")
yield entity.getUser

```

IMPORTANT: For some operations Farango needs that you define in the Config object the property `rootPassword`, because in these situations Farango needs to connect using the `root` account.

### Creation or connecting to a database

For this purpose you have to use the `DB.database(String | DBCreateOptions)` method, you can inform a database name for example:

```scala

for
  database <- db.database("application-database")
  ...

```

Or you can use a [`DBCreateOptions`](https://www.javadoc.io/static/com.arangodb/arangodb-java-driver/7.1.0/com/arangodb/model/DBCreateOptions.html) to create an instance of `one.estrondo.farango.Database`










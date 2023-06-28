# Farango

A small Functional Scala 3 wrapper for [ArangoDB](http://www.arangodb.com).

| Scaladex   | Scaladoc   |  Maven  |
| :--------: | :--------: | :-----: |
| [![farango Scala version support](https://index.scala-lang.org/estrondo/farango/farango/latest.svg)](https://index.scala-lang.org/estrondo/farango/farango) |  [![javadoc](https://javadoc.io/badge2/one.estrondo/farango_3/javadoc.svg)](https://javadoc.io/doc/one.estrondo/farango_3)  | ![Maven Central](https://img.shields.io/maven-central/v/one.estrondo/farango_3) |

## Why?

So, I have been working on a project that utilises ArangoDB and [ZIO](http://www.zio.dev). I would like to use Arango due to its geographical index support. I'm enjoying coding in functional fashion. Therefore, after some mess and some ideas I have decide to move all codes to an external library and publish it. So, it's the very beginning of this project and I have just done the basic set of features and functionalities to help me in my project. Any help such as ideas, features and fixes are welcome.

## Functional Programming

Farango(_Functional Arango_) has built in support for [ZIO](http://www.zio.dev), [Cats Effect](https://typelevel.org/cats-effect), Scala's Future, Try and Either[Throwable, _] types.

## Overview

### Starting

Add in your `build.sbt` one of the following dependencies:

```scala

"one.estrondo.farango" %% "farango" % "0.0.1" // If you want to use just the Scala's types.

"one.estrondo.farango" %% "farango-zio" % "0.0.1" // If you want working with ZIO.

"one.estrondo.farango" %% "farango-cats-effect" % "0.0.1" // If you want Cats Effect.

```

Farango was written with functional programming style kept in mind. Therefore, it uses the famous Effectfull type `F[_]`. In order to use Farango you have to make some imports in your code.

```scala

import one.estrondo.farango.*

import one.estrondo.farango.zio.given // If you are using ZIO.

import one.estrondo.farango.cats.effect.given // it you are using Cats Effect.

```

### Examples

All examples mentioned above could be found on [ZIO example](https://github.com/estrondo/farango/blob/main/examples/src/main/scala/one/estrondo/farango/examples/ZIOExample.scala) and [Cats Effect example](https://github.com/estrondo/farango/blob/main/examples/src/main/scala/one/estrondo/farango/examples/CatsEffectExample.scala).

### Creating a DB instance.

Farango strives to resemble the ArangoDB's Java Drive design, but to have the ability of create/recreate a database as many times as you want Farango needs to keep all configuration information, because of this you have to create a Farango Config object (`one.estrondo.farango.Config`).

```scala

val config = Config()
  .addHost("localhost", 8529)
  .withUser("user")
  .withPassword("user password")
  .withRootPassword("Arango's root password") // You need this in order to create databases and collections through Farango.

```

After that it's time to have some fun, or at least attempt to do so. To create a Farango's DB version of Arango's DB you currently need to utilise the object `one.estrondo.farango.SyncDB`. Prior to version 7.x ArangoDB Java Driver had `async` and `sync` clients. However, in the current version of ArangoDB Java Driver only supports a `sync` client, in their website they say the support to `async` will be re-added in a future version 7.x. Hence, Farango provides a `SyncDB` object which acts as a Factory for the `one.estrondo.farango.DB`.

Let's remember one small thing, in ArangoDB Java Driver a DB instance represents not a specific database, but it represents the database server.

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

You are free to choose, or you could use the `scala.util.Try` directly or map it to a `scala.util.Either` object.

You had made your choice of which framework to use to build your application, let's see some examples.

### Creating a new user.

Let's assume that we want to create a user on the database server. This can be accomplished with `createUser(user, password, options)` method. The parameter `options` is a [`UserCreateOptions`](https://www.javadoc.io/doc/com.arangodb/arangodb-java-driver/latest/com/arangodb/model/UserCreateOptions.html) and the result type is [`UserEntity`](https://www.javadoc.io/doc/com.arangodb/arangodb-java-driver/latest/com/arangodb/entity/UserEntity.html) both from ArangoDB Java Driver. Actually, the result type is a `F[UserEntity]`. The parameter `options` is optional.

```scala

for
  entity <- db.createUser("user-login", "user-password")
yield entity.getUser

```

IMPORTANT: For some operations Farango needs that you define in the Config object the property `rootPassword`, because in these situations Farango needs to connect to the database server using the `root` account.

### Creating a database object.

For this purpose you have to use the `DB.database(String | DBCreateOptions)` method, you can inform a database name for example:

```scala

for
  database <- db.database("application-database")
  ...

```

Or you can use a [`DBCreateOptions`](https://www.javadoc.io/static/com.arangodb/arangodb-java-driver/7.1.0/com/arangodb/model/DBCreateOptions.html) to create an instance of `one.estrondo.farango.Database`.

```scala
for
  database <- db.database(
                DBCreateOptions()
                .name("test-database")
                .options(DatabaseOptions().sharding("sharding"))
              )
  ...
```

### Creating a database on the database server.

Creating a database on database server is straightforward with Arango Java Driver. The same applies to the Farango. Simply utilise the `Database.create()` method. It is worth noting that Farango needs that `Config` object with a `rootPassword` defined.

```scala

for
  database <- db.database("test-database")
                .create()
  ...

```

### Collections.

Currently Farango only supports document collections.To create a `one.estrondo.farango.Collection` you simply need to utilise the `database.collection(name, indexes, options)`method. Note that indexes is a `Seq[one.estrondo.farango.IndexDescription]` and options is a [`CollectionCreateOptions`](https://www.javadoc.io/static/com.arangodb/arangodb-java-driver/7.1.0/com/arangodb/model/CollectionCreateOptions.html). Both indexes and options are optional.

```scala

val indexes = Seq(IndexDescription.Geo(Seq("geom"), GeoIndexOptions().geoJson(true)))

val collection = database
                  .collection("collection-name", indexes)

```

### Creating a collection on the database server.

As we did with our database, to create a collection on the database server you can utilise the `collection.create()` method.

```scala

for
  collection <- database.collection("collection-name").create()
  ...

```

## Mr Data.

Now we will go through how to create, read, update and delete our documents.

Farango aims to help with the separation between the business layer and the storage layer. To accomplish this Farango employs the mapping process, or transformation. Please, refer the [the following](#inserting-a-document) section to understand how Farango accomplishes this.

### Inserting a document.

Let's assume we have a document in our business layer which of the type `T`, and we want to represent this document in our storage layer as type `A`. Furthermore, we want after inserting this document return it as type `R`.

To insert a document into the collection, you can use `collection.insertDocument` method.

```scala

for
  entity <- collection.insertDocument[A, R](value) // The type of value is T.
  ...

```

The method `insertDocument` will receive a value of the type T, and it will convert to `A` and store it in the collection. After that it will return a entity that is the type [`DocumentCreateEntity[R]`](https://www.javadoc.io/doc/com.arangodb/arangodb-java-driver/latest/com/arangodb/entity/DocumentCreateEntity.html), it is worth noting that is a `DocumentCreateEntity` of `R`.

The method `insertDocument` is expecting for two `given` objects, `one.estrondo.farango.FarangoTransformer[T, A]` and `one.estrondo.farango.FarangoTransformer[A, R]`. You can provide your own given objects that implement this Typeclasse, it may feel rather uncomfortable to do so. Imagine an application with a bunch of types and transformers, it is going to be a mess! There is where our friend [Ducktape](https://github.com/arainko/ducktape) comes to rescue, thanks Ducktape!

### Ducktape comes to assist us.

Ducktape as the creators say is _"ducktape is a library for boilerplate-less and configurable transformations between case classes and enums/sealed traits for Scala 3. Directly inspired by chimney."_

First add Farango's ducktape extension in your `build.sbt`.

```scala

libraryDependencies += "one.estrondo" %% "farango-ducktape" % "0.0.1"

```

Once you have added Farango's ducktape extension to your build you can use it in two basic ways. First, you can import `one.estrondo.farango.ducktape.given` and where Farango needs a `FarangoTransformer` one `given` object will be provided automatically, but it is worth attention. Farango utilises ducktape to generate a `Transformer`, so please refer to the [ducktape documention](https://github.com/arainko/ducktape#1-case-class-to-case-class).

Let's have a look at an example:

```scala

import one.estrondo.farango.ducktape.given

val postIt = PostIt("My Post-it")

for
  createEntity <- collection
                    .insertDocument[StoredPostIt, CreatedPostIt](postIt)
  ...

```

In the example above we are inserting a document of type `PostIt`, it will be transformed into a `StoredPostIt` automatically and after that the `StoredPostIt` value from the collection will be transformed into a `CreatedPostIt` as well.

So, if you require more control or it is impossible to create an automatic transformer, you can configure a new one using the object `one.estrondo.farango.ducktape.DucktapeTransformer`. Please, read [ducktape's documentation](https://github.com/arainko/ducktape#3-case-class-to-case-class-with-config) to be introduced.

An example configuring a new Transformer.

```scala

import one.estrondo.farango.ducktape.DucktapeTransformer

val applePostIt = ApplePostIt("My Post-it too.")

given FarangoTransformer[ApplePostIt, StoredPostIt] = DucktapeTransformer[ApplePostIt, StoredPostIt](
  Field.renamed(_.id, _.ID)
)

for
  createEntity <- collection
                    .insertDocument[StoredPostIt, CreatedPostIt](applePostIt)

  ...

```

In the example above we are receiving an `ApplePostIt` document which has an attribute 'ID' instead 'id' as in `StoredPostIt`, because of this ducktape can't create an automatic transformer. Hence, we have to provide one semi-automatic transformer, in the example we simply inform ducktape that the attribute 'ID' in `ApplePostIt` was renamed as 'id' in `StoredPostIt`.

### Getting a document.

Example.

```scala

for
  getPostIt <- collection.getDocument[StoredPostIt, PostIt](key) // It returns a F[Option[PostIt]]

  ...

```

### Querying documents.

Farango returns all queries as Streams.

#### Querying with ZIO.

Farango returns a `zio.stream.ZStream[Any, Throwable, R]`.

```scala

import one.estrondo.farango.zio.given

for
  result <- database
              .query[StoredPostIt, PostIt](
                "FOR postIt IN @@collection FILTER postIt.id == @id RETURN postIt",
                Map(
                  "@collection" -> "collection",
                  "id"          -> postIt.id
                )
              )
              .runCollect

  ...

```

In the example above Farango returns a `ZStream[Any, Throwable, PostIt]`, note we collecting all objects for the example's sake.

#### Querying with Cats Effect.

Farango returns a `fs2.Stream[IO, R]`.

```scala

import one.estrondo.farango.cats.effect.given

for
  result <- database
              .query[StoredPostIt, PostIt](
                "FOR postIt IN @@collection FILTER postIt.id == @id RETURN postIt",
                Map(
                  "@collection" -> "collection",
                  "id"          -> postIt.id
                )
              )
              .compile
              .toList

  ...

```

In the example above Farango returns a `fs2.Stream[IO, PostIt]`, note we are converting the stream to a list for the example's sake.

### Updating documents.

Let's have a look at an example:

```scala

val postIt = PostIt("My Post-it")
val newLastUpdate = LocalDateTime.now()

for
  updateEntity <- collection.updateDocument[StoredPostIt, UpdateContent, UpdatedPostIt](
                    documentKey,
                    postIt.copy(content = "New Content", lastUpdate = newLastUpdate),
                    DocumentUpdateOptions()
                      .returnOld(true)
                      .returnNew(true)
                  )

  _ = assert(updateEntity.getOld == UpdatedPostIt(postIt.id, "My Post-it"))
  _ = assert(updateEntity.getNew == UpdatedPostIt(postIt.id, "New Content"))

  ...

```

In the example above Farango transforms a `PostIt` into `UpdateContent` and partially updating the document in the collection, see [ArangoDB Java Driver documentation](<https://www.javadoc.io/doc/com.arangodb/arangodb-java-driver/latest/com/arangodb/ArangoCollection.html#updateDocument(java.lang.String,java.lang.Object,com.arangodb.model.DocumentUpdateOptions,java.lang.Class)>).

### Deleting documents.

Example:

```scala

val postIt = PostIt("My Post-it")
val newLastUpdate = LocalDateTime.now()

// document was updated.

for
  deleteEntity <- collection.deleteDocument[StoredPostIt, DeletedPostIt](
                    documentEntity,
                    DocumentDeleteOptions().returnOld(true)
                  )

  _ = assert(deleteEntity.getOld == DeletedPostIt(postIt.id, "New Content", newLastUpdate))

  ...

```

## Contributions

Farango is in its early stages, for example, it currently supports only document collections, there is no support for edge collections and there are many functionalities that Arango's Java Driver provides which are not covered yet by Farango. If you believe this project could be helpful and you would like to contribute, your help is more than welcome.

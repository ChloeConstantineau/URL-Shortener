package io.shortener

import io.shortener.repo.ShortLinkRepoRedisImpl
import zio.*
import zio.redis.*
import zio.schema.Schema
import zio.schema.codec.{BinaryCodec, ProtobufCodec}
import zio.test.*
import zio.test.Assertion.*

import java.net.URI

object ShortLinkRepoRedisImplSpec extends ZIOSpecDefault:

    def spec =
      suite("ShortLinkRepoRedisImpl")(
        test("Should register a valid slug") {
          for
              redis              <- ZIO.service[Redis]
              repo               <- ZIO.service[ShortLinkRepoRedisImpl]
              registerResponse   <- repo.register(myUrl)
              maybeRegisteredUrl <- redis.get(registerResponse.slug.value).returning[String]
          yield assertTrue(maybeRegisteredUrl.isDefined, maybeRegisteredUrl.get == myUrl.toString)

        },
        test("Should lookup a valid slug") {
          for
              redis        <- ZIO.service[Redis]
              repo         <- ZIO.service[ShortLinkRepoRedisImpl]
              registered   <- repo.register(myUrl)
              lookupString <- repo.lookup(registered.slug.value)
              maybeLookup  <- redis.get(registered.slug.value).returning[String]
          yield assertTrue(lookupString.isDefined, lookupString.map(_.toString) == maybeLookup)
        },
        test("Should return `None` for slug that is not registered") {
          for
              redis  <- ZIO.service[Redis]
              repo   <- ZIO.service[ShortLinkRepoRedisImpl]
              noResp <- repo.lookup("Some slug")
          yield assertTrue(noResp.isEmpty)
        },
        test("Should fail to register if redis throws") {
          for
              redis <- ZIO.service[Redis]
              repo  <- ZIO.service[ShortLinkRepoRedisImpl]
              _     <- redis.set(counterKey, "invalidNumber")
              error <- repo.register(myUrl).exit
          yield assert(error)(Assertion.failsWithA[Throwable])

        } @@ TestAspect.after(resetCounterKey()),
        test("Should fail to lookup if registered slug is not a valid URL") {
          for
              redis      <- ZIO.service[Redis]
              repo       <- ZIO.service[ShortLinkRepoRedisImpl]
              registered <- repo.register(myUrl)
              _          <- redis.set(registered.slug.value, "I'm not a url")
              error      <- repo.lookup(registered.slug.value).exit
          yield assert(error)(Assertion.failsWithA[Throwable])
        },
      ).provideShared(
        layers,
      ) @@ TestAspect.sequential

    // * Utils *//

    private val myUrl      = URI("http://google.com").toURL
    private val counterKey = "myKey"

    private val redisLayer =
      ZLayer.succeed[CodecSupplier](new CodecSupplier {
        def get[A: Schema]: BinaryCodec[A] = ProtobufCodec.protobufCodec
      }) >>> Redis.local

    private val layers = redisLayer >+> ZLayer.fromZIO(for
        slugGen <- SlugGeneratorMock.live
        repo    <- ShortLinkRepoRedisImpl.live(slugGen, counterKey)
    yield repo)

    private def resetCounterKey(): ZIO[Redis, RedisError, Unit] =
      for
          redis <- ZIO.service[Redis]
          _     <- redis.del(counterKey)
      yield ()

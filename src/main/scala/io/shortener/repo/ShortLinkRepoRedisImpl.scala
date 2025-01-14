package io.shortener.repo

import io.shortener.models.*
import zio.*
import zio.redis.Redis
import io.shortener.slug.SlugGenerator

import java.net.{URI, URL}

final case class ShortLinkRepoRedisImpl(
    redis: Redis,
    slugGenerator: SlugGenerator,
    counterKey: String,
) extends ShortLinkRepo:

    override def register(url: URL): Task[RegisterResponse] =
      for {
        count <- redis.incr(counterKey)
        slug  <- slugGenerator.encode(count)
        _     <- redis.set(slug.value, url.toString)
      } yield RegisterResponse(slug, url)

    override def lookup(slug: String): Task[Option[URL]] =
      redis
        .get(slug)
        .returning[String]
        .foldZIO(
          e => ZIO.fail(new Exception(e.getMessage)),
          res => ZIO.succeed(res.map(URI(_).toURL)),
        )

object ShortLinkRepoRedisImpl:
    def live(slugGenerator: SlugGenerator, counterKey: String): ZIO[Redis, Nothing, ShortLinkRepoRedisImpl] =
      for redis <- ZIO.service[Redis]
      yield ShortLinkRepoRedisImpl(redis, slugGenerator, counterKey)

package io.shortener.repo

import io.shortener.service.SlugGenerator
import io.shortener.models.*
import zio.*
import zio.redis.Redis
import java.net.{URI, URL}

private final case class ShortLinkRepoRedisImpl(
    redis: Redis,
    slugGenerator: SlugGenerator,
    counterKey: String,
) extends ShortLinkRepo:

    override def register(url: URL): Task[RegisterResponse] =
      for {
        count <- redis.incr(counterKey)
        slug  <- slugGenerator.generateSlug(count.toInt)
        _     <- redis.set(slug, url.toString)
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

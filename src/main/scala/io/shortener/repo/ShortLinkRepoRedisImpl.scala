package io.shortener.repo

import io.shortener.models.*
import io.shortener.slug.SlugGeneratorT
import zio.*
import zio.redis.Redis

import java.net.{URI, URL}

final case class ShortLinkRepoRedisImpl(
    redis: Redis,
    slugGenerator: SlugGeneratorT,
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
          res =>
            res.fold(ZIO.succeed(None))(s =>
              ZIO
                .attempt(URI(s).toURL)
                .foldZIO(
                  e => ZIO.logError(s"Registered slug `$s` is not a valid URL") *> ZIO.fail(e),
                  r => ZIO.succeed(Some(r)),
                ),
            ),
        )

object ShortLinkRepoRedisImpl:
    def live(slugGenerator: SlugGeneratorT, counterKey: String): ZIO[Redis, Nothing, ShortLinkRepoRedisImpl] =
      for redis <- ZIO.service[Redis]
      yield ShortLinkRepoRedisImpl(redis, slugGenerator, counterKey)

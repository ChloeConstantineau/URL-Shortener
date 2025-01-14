package io.shortener

import io.shortener.AppConfig.serverConfig
import io.shortener.repo.{ShortLinkRepo, ShortLinkRepoRedisImpl}
import io.shortener.routes.AppRoutes
import io.shortener.slug.{Alphabet, SlugGenerator}
import zio.*
import zio.config.typesafe.*
import zio.http.*
import zio.redis.*
import zio.schema.*
import zio.schema.codec.*

object Main extends ZIOAppDefault:

    override val bootstrap: ZLayer[ZIOAppArgs, Any, Any] =
      Runtime.setConfigProvider(ConfigProvider.fromResourcePath())

    private val redisLayer =
      (AppConfig.redisConfig ++ ZLayer.succeed[CodecSupplier](new CodecSupplier {
        def get[A: Schema]: BinaryCodec[A] = ProtobufCodec.protobufCodec
      })) >>> Redis.singleNode

    private val repoLayer =
      ZLayer.fromZIO:
        for
            config        <- ZIO.config[AppConfig]
            alphabet      <- Alphabet.live(config.genConfig.alphabet)
            slugGenerator <- SlugGenerator.live(alphabet)
            repo          <- ShortLinkRepoRedisImpl.live(slugGenerator, config.counterKey)
        yield repo

    private val server = serverConfig.project(s => Server.Config.default.port(s.port)) >>> Server.live

    private val layers = (redisLayer >>> repoLayer) ++ serverConfig ++ server

    def run: ZIO[Any, Throwable, Nothing] =
      Server
        .serve(AppRoutes.routes)
        .provide(layers)

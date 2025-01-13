package io.shortener

import io.shortener.repo.{ShortLinkRepo, ShortLinkRepoRedisImpl}
import io.shortener.routes.AppRoutes
import io.shortener.service.SlugGeneratorImpl
import zio.*
import zio.redis.*
import zio.config.typesafe.*
import zio.http.*
import zio.schema.*
import zio.schema.codec.*
import io.shortener.AppConfig.serverConfig

object Main extends ZIOAppDefault:

    override val bootstrap: ZLayer[ZIOAppArgs, Any, Any] =
      Runtime.setConfigProvider(ConfigProvider.fromResourcePath())

    private val redisLayer =
      (AppConfig.redisConfig ++ ZLayer.succeed[CodecSupplier](new CodecSupplier {
        def get[A: Schema]: BinaryCodec[A] = ProtobufCodec.protobufCodec
      })) >>> Redis.singleNode

    private val shortLinkRepoRedis =
      for
          config        <- ZIO.config[AppConfig]
          slugGenerator <- SlugGeneratorImpl.live(config.slugGeneratorConfig)
          repo          <- ShortLinkRepoRedisImpl.live(slugGenerator, config.counterKey)
      yield repo

    private val server = serverConfig.project(s => Server.Config.default.port(s.port)) >>> Server.live

    private val layers = (redisLayer >>> ZLayer.fromZIO(shortLinkRepoRedis)) ++ serverConfig ++ server

    def run: ZIO[Any, Throwable, Nothing] =
      Server
        .serve(AppRoutes.routes)
        .provide(layers)

    // TODO Tests

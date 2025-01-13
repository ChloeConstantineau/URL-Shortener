package io.shortener

import zio.*
import zio.config.magnolia.*
import zio.Config
import zio.redis.RedisConfig


case class ServerConfig(url: String, port: Int)
case class SlugGeneratorConfig(alphabet: String)

case class AppConfig(
    counterKey: String,
    server: ServerConfig,
    redis: RedisConfig,
    slugGeneratorConfig: SlugGeneratorConfig,
) derives Config

object AppConfig:
    private val config = ZLayer(ZIO.config(deriveConfig[AppConfig]))

    val redisConfig: ZLayer[Any, Config.Error, RedisConfig]   = config.project(_.redis)
    val serverConfig: ZLayer[Any, Config.Error, ServerConfig] = config.project(_.server)

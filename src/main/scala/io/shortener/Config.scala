package io.shortener

import zio.{Config, *}
import zio.config.magnolia.*
import zio.redis.RedisConfig

case class ServerConfig(url: String, port: Int)
case class SlugGeneratorConfig(
    alphabet: Option[String],
    counterKey: String,
)

case class AppConfig(
    redis: RedisConfig,
    server: ServerConfig,
    slugGenerator: SlugGeneratorConfig,
) derives Config

object AppConfig:
    private val config = ZLayer(ZIO.config(deriveConfig[AppConfig]))

    val redisConfig: ZLayer[Any, Config.Error, RedisConfig]   = config.project(_.redis)
    val serverConfig: ZLayer[Any, Config.Error, ServerConfig] = config.project(_.server)

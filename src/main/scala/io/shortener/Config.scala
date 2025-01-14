package io.shortener

import zio.*
import zio.config.magnolia.*
import zio.Config
import zio.redis.RedisConfig

case class ServerConfig(url: String, port: Int)
case class SlugGeneratorOptions(
    alphabet: Option[String],
)

case class AppConfig(
    counterKey: String,
    genConfig: SlugGeneratorOptions,
    redis: RedisConfig,
    server: ServerConfig,
) derives Config

object AppConfig:
    private val config = ZLayer(ZIO.config(deriveConfig[AppConfig]))

    val redisConfig: ZLayer[Any, Config.Error, RedisConfig]   = config.project(_.redis)
    val serverConfig: ZLayer[Any, Config.Error, ServerConfig] = config.project(_.server)

package io.shortener.routes

import io.shortener.ServerConfig
import io.shortener.repo.ShortLinkRepo
import zio.*
import zio.http.*
import zio.http.codec.*
import zio.http.codec.PathCodec.path
import zio.http.endpoint.openapi.*

object AppRoutes:
    private val openAPI = OpenAPIGen.fromEndpoints(
      title = "URL Shortener API",
      version = "1.0",
      GetShortLinkRoute.endpoint,
      PostShortLinkRoute.endpoint,
    )

    private val swaggerRoutes = SwaggerUI.routes("openapi", openAPI)

    val routes: Routes[ShortLinkRepo & ServerConfig, Response] =
      GetShortLinkRoute.route ++ PostShortLinkRoute.route ++ swaggerRoutes

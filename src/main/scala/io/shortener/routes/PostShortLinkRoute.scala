package io.shortener.routes

import io.shortener.ServerConfig
import io.shortener.models.*
import io.shortener.repo.ShortLinkRepo
import zio.*
import zio.http.*
import zio.http.codec.*
import zio.http.codec.PathCodec.path
import zio.http.endpoint.*
import zio.http.endpoint.AuthType.None

import java.net.URI
import scala.language.postfixOps

object PostShortLinkRoute:

    val endpoint: Endpoint[Unit, ShortLinkRequest, AppError, ShortLinkResponse, None] =
      Endpoint((RoutePattern.POST / "api" / "v1" / "shorten") ?? Doc.p("Route to register a shortened URL"))
        .in[ShortLinkRequest]
        .out[ShortLinkResponse]
        .outErrors[AppError](
          HttpCodec.error[InvalidUrlError](Status.BadRequest) ?? Doc.p(
            "[Error] Trying to register a value that is not a URL",
          ),
          HttpCodec.error[InternalError](Status.InternalServerError),
        )
        .examplesIn(
          (
            "Example",
            ShortLinkRequest(destination = java.net.URI("http://my-very-very-very-long-url.com").toURL),
          ),
        )
        .examplesOut(
          (
            "Example",
            ShortLinkResponse(
              shortLink = java.net.URI("http://tiny.io/my-slug").toURL,
              destination = java.net.URI("http://my-very-very-very-long-url.com").toURL,
            ),
          ),
        )

    private val routeHandler = handler: (req: ShortLinkRequest) =>
        for
            _          <- ZIO.logInfo(s"[POST] /shorten endpoint called")
            serverUrl  <- ZIO.service[ServerConfig].map(_.url)
            repo       <- ZIO.service[ShortLinkRepo]
            registered <- repo
                            .register(req.destination)
                            .catchAll(e => {
                              val errorMessage = s"Failed to register ShortLinkRequest $e"
                              ZIO.logError(errorMessage) *>
                                ZIO.fail(InternalError(errorMessage))
                            })
            response   <-
              ZIO
                .attempt(URI(serverUrl + "/" + registered.slug.value).toURL)
                .foldZIO(
                  e => ZIO.logError(e.getMessage) *> ZIO.fail(InternalError(e.getMessage)),
                  su => ZIO.succeed(ShortLinkResponse(su, registered.destination)),
                )
        yield response

    val route: Routes[ShortLinkRepo & ServerConfig, Nothing] =
      endpoint.implementHandler(routeHandler).toRoutes

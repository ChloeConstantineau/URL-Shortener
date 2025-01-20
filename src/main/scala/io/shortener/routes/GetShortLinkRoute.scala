package io.shortener.routes

import io.shortener.models.*
import io.shortener.repo.ShortLinkRepo
import zio.*
import zio.http.*
import zio.http.codec.*
import zio.http.endpoint.*
import zio.http.endpoint.AuthType.None
import zio.schema.Schema

import java.net.URI

object GetShortLinkRoute:

    val endpoint: Endpoint[String, String, AppError, Header.Location, None] =
      Endpoint(
        RoutePattern.GET / PathCodec.string("slug") ?? Doc.p("Route to be redirected to your destination URL"),
      )
        .out[Unit](Status.SeeOther)
        .outHeader(HttpCodec.location)
        .outErrors[AppError](
          HttpCodec.error[InvalidUrlError](Status.InternalServerError) ?? Doc
            .p("[Error] Trying to redirect a destination that is not a URL"),
          HttpCodec.error[InternalError](Status.InternalServerError),
          HttpCodec.error[NotFoundError](Status.NotFound),
        )
        .examplesIn(("Example", "mySlug"))

    private val routeHandler: Handler[ShortLinkRepo, AppError, String, Header.Location] =
      handler: (slug: String) =>
          for
              repo           <- ZIO.service[ShortLinkRepo]
              maybeShortLink <- repo
                                  .lookup(slug)
                                  .catchAll(e =>
                                      val errorMessage = s"Failed to lookup slug; $e"
                                      ZIO
                                        .logError(errorMessage) *>
                                        ZIO.fail(InternalError(errorMessage)),
                                  )
              response       <-
                maybeShortLink.fold(ZIO.fail(NotFoundError(s"Not Found: $slug")))(shortLink =>
                  URL
                    .fromURI(URI(shortLink.toString))
                    .fold(
                      ZIO.fail(
                        InvalidUrlError(
                          s"Registered URI $shortLink is invalid",
                          shortLink.toString,
                        ),
                      ),
                    )(url =>
                      ZIO.logInfo(s"Redirecting to $url") *>
                        ZIO.succeed(Header.Location(url)),
                    ),
                )
          yield response

    val route: Routes[ShortLinkRepo, Nothing] = endpoint.implementHandler(routeHandler).toRoutes

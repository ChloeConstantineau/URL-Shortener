package io.shortener.models

import zio.schema.{DeriveSchema, Schema}
import zio.schema.annotation.description

import java.net.URL

case class ShortLinkResponse(
    @description("Shortened URL")
    shortLink: URL,
    @description("Redirect destination")
    destination: URL,
)

object ShortLinkResponse:
    implicit val schema: Schema[ShortLinkResponse] = DeriveSchema.gen

package io.shortener.models

import zio.schema.{DeriveSchema, Schema}

import java.net.URL

case class ShortLinkRequest(destination: URL)

object ShortLinkRequest:
    implicit val schema: Schema[ShortLinkRequest] =
      DeriveSchema.gen[ShortLinkRequest]

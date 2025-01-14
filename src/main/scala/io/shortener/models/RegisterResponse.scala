package io.shortener.models

import zio.schema.{DeriveSchema, Schema}
import zio.schema.annotation.description

import java.net.URL

case class RegisterResponse(
    slug: Slug,
    destination: URL,
)

object RegisterResponse:
    implicit val schema: Schema[RegisterResponse] = DeriveSchema.gen

package io.shortener.models

import zio.schema.{DeriveSchema, Schema}

import scala.annotation.nowarn

trait AppError(@nowarn message: String)
case class InvalidUrlError(message: String, invalidUrl: String) extends AppError(message)
case class InternalError(message: String)                       extends AppError(message)
case class NotFoundError(message: String)                       extends AppError(message)

object InvalidUrlError:
    implicit val invalidURLSchema: Schema[InvalidUrlError] = DeriveSchema.gen[InvalidUrlError]

object InternalError:
    implicit val internalErrorSchema: Schema[InternalError] = DeriveSchema.gen[InternalError]

object NotFoundError:
    implicit val notFoundErrorSchema: Schema[NotFoundError] = DeriveSchema.gen[NotFoundError]

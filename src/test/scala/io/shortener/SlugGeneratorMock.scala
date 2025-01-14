package io.shortener

import io.shortener.models.Slug
import io.shortener.slug.SlugGeneratorT
import zio.{UIO, ZIO}

final case class SlugGeneratorMock() extends SlugGeneratorT:
    override def encode(num: Long): UIO[Slug] = ZIO.succeed(Slug("valid slug"))

object SlugGeneratorMock:
    def live: UIO[SlugGeneratorT] = ZIO.succeed(SlugGeneratorMock())

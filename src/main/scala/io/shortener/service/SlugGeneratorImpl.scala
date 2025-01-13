package io.shortener.service

import io.shortener.SlugGeneratorConfig
import zio.{UIO, ZIO}

private final case class SlugGeneratorImpl(slugGeneratorConfig: SlugGeneratorConfig) extends SlugGenerator:

    private val alphabet = slugGeneratorConfig.alphabet
    private val base     = alphabet.length()

    override def generateSlug(count: Int): UIO[String] =
      if count < base then ZIO.succeed(alphabet.charAt(count).toString())
      else
          generateSlug((count / base) - 1)
            .map(_ + alphabet.charAt(count % base))

object SlugGeneratorImpl:
    def live(config: SlugGeneratorConfig): UIO[SlugGenerator] =
      ZIO.succeed(SlugGeneratorImpl(config))

package io.shortener.service

import zio.UIO

trait SlugGenerator:
    def generateSlug(count: Int): UIO[String]

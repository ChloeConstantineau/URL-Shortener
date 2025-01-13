package io.shortener.repo

import zio.*
import io.shortener.models.*
import java.net.URL

trait ShortLinkRepo:
    def lookup(slug: String): Task[Option[URL]]
    def register(url: URL): Task[RegisterResponse]

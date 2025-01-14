package io.shortener.repo

import io.shortener.models.*
import zio.*

import java.net.URL

trait ShortLinkRepo:
    def lookup(slug: String): Task[Option[URL]]
    def register(url: URL): Task[RegisterResponse]

package io.shortener.services

import zio.*

sealed abstract case class Alphabet(value: Vector[Char]):
    def length: Int = value.length

object Alphabet:
    def minLength: Int = 3

    def live(config: Option[String]): UIO[Alphabet] =
        val fallbackDefault = (e: String) =>
          ZIO.logError(s"[Error] $e ; falling back to default alphabet") *> ZIO.succeed(default)

        config match
            case Some(value) =>
              value match
                  case v if v.distinct.length != v.length =>
                    fallbackDefault("Alphabet is not composed of unique characters")
                  case v if v.getBytes.length != v.length =>
                    fallbackDefault("Alphabet contains multibyte characters")
                  case v if v.length < minLength          =>
                    fallbackDefault(s"Alphabet has too few characters, it should contain at least $minLength")
                  case v                                  =>
                    ZIO.succeed(new Alphabet(v.toVector) {})
            case None        => ZIO.succeed(default)

    def default: Alphabet =
      new Alphabet((('a' to 'z') ++ ('A' to 'Z') ++ ('0' to '9')).toVector) {}

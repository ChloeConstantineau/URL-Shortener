package io.shortener.slug

import zio.*

final case class Alphabet(value: Vector[Char]):
    def base: Int = value.length

object Alphabet:
    def MIN_LENGTH: Int = 3

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
                  case v if v.length < MIN_LENGTH         =>
                    fallbackDefault(
                      s"Alphabet has too few characters, it should contain at least $MIN_LENGTH",
                    )
                  case v                                  =>
                    ZIO.succeed(Alphabet(v.toVector))
            case None        => ZIO.succeed(default)

    def shuffle(a: Alphabet): Alphabet =
      Alphabet(
        a.value.indices
          .foldLeft(a.value) { (vec, i) =>
              val j: Int = a.base - 1 - i
              val r: Int = (i * j + vec(i) + vec(j)) % a.base
              val iChar  = vec(i)
              vec
                .updated(i, vec(r))
                .updated(r, iChar)
          },
      )

    def default: Alphabet =
      Alphabet((('a' to 'z') ++ ('A' to 'Z') ++ ('0' to '9')).toVector)

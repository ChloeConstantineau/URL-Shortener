package io.shortener.slug

import zio.*

import scala.annotation.tailrec

final case class Alphabet(value: Vector[Char]):
    private val length = value.length

    def dropHead(): Alphabet = Alphabet(value.tail)

    def validId(id: String): Boolean =
      id.forall(c => value.contains(c))

    def toId(num: Long): String =
        @tailrec
        def go(num: Long, acc: List[Char]): String =
            if num <= 0 then acc.mkString
            else go(num / length, value((num % length).toInt) :: acc)

        if num <= 0 then ""
        else go(num / length, List(value((num % length).toInt)))

    def fillToMinLength(id: String, minLength: Int): String =
      id + value.take(math.min(minLength - id.length, length)).mkString

    def toNumber(id: String): Long =
      id.foldLeft(0L)((acc, c) => acc * length + indexOf(c).toLong)

    def shuffle: Alphabet =
      Alphabet(
        value.indices
          .foldLeft(value) { (vec, i) =>
              val j: Int = length - 1 - i
              val r: Int = (i * j + vec(i) + vec(j)) % length
              val iChar  = vec(i)
              vec
                .updated(i, vec(r))
                .updated(r, iChar)
          },
      )

    def offsetFromPrefix(prefix: Char): Int = indexOf(prefix)

    def rearrange(offset: Int): Alphabet =
      Alphabet(value.drop(offset) ++ value.take(offset))

    def rearrange(numbers: List[Long], increment: Int): Alphabet =
        val offset =
          (numbers.indices.foldLeft(numbers.length) { (offset, i) =>
              val atIndex = (numbers(i) % length).toInt
              offset + i + value(atIndex)
          } % length) + increment
        rearrange(offset)

    def reverse: Alphabet = Alphabet(value.reverse)

    private def indexOf(c: Char): Int = value.indexOf(c.toInt)

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

    def default: Alphabet =
      Alphabet((('a' to 'z') ++ ('A' to 'Z') ++ ('0' to '9')).toVector)

package io.shortener.slug

import io.shortener.models.Slug
import zio.*

import scala.annotation.tailrec

trait SlugGeneratorT:
    def encode(num: Long): UIO[Slug]

final case class SlugGenerator(alphabet: Alphabet) extends SlugGeneratorT:

    override def encode(num: Long): UIO[Slug] =
        @tailrec
        def go(num: Long, acc: List[Char]): String =
          if num <= 0 then acc.mkString
          else go(num / alphabet.base, alphabet.value((num % alphabet.base).toInt) :: acc)

        val posNum = Math.max(0, num)
        val slug   = go(posNum / alphabet.base, List(alphabet.value((posNum % alphabet.base).toInt)))

        ZIO.succeed(Slug(slug))

object SlugGenerator:
    def live(alphabet: Alphabet): UIO[SlugGenerator] =
        val shuffledAlphabet = Alphabet.shuffle(alphabet)
        ZIO.succeed(SlugGenerator(shuffledAlphabet))

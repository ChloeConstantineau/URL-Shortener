package io.shortener

import io.shortener.slug.{Alphabet, SlugGenerator}
import zio.{Scope, ZIO}
import zio.test.*

object SlugGeneratorSpec extends ZIOSpecDefault:

    def spec: Spec[TestEnvironment & Scope, Any] =
      suite("SlugGeneratorSpec")(
        test("Should generate a valid slug with any `num` value") {
          check(Gen.long): num =>
              for
                  gen  <- getGen(Some(smallAlphabet))
                  slug <- gen.encode(num)
              yield assert(slug.value)(Assertion.isNonEmptyString)
        },
        test("Should have a slug longer than 1 when num exceeds last index of the alphabet") {
          for
              gen  <- getGen(Some(smallAlphabet))
              slug <- gen.encode(gen.alphabet.value.length)
          yield assert(slug.value.length)(Assertion.isGreaterThan(1))
        },
        test("Should generate a valid slug with any valid `alphabet`") {
          check(Gen.string(Gen.asciiChar), Gen.long) { (a, num) =>
            for
                alphabet <- Alphabet.live(Some(a))
                gen      <- SlugGenerator.live(alphabet)
                slug     <- gen.encode(num)
            yield assert(slug.value)(Assertion.isNonEmptyString)
          }
        },
      ) @@ TestAspect.silentLogging

    private val smallAlphabet                                                         = "aAbBcCdDeE"
    private def getGen(alphaString: Option[String]): ZIO[Any, Nothing, SlugGenerator] =
      for
          alphabet <- Alphabet.live(alphaString)
          gen      <- SlugGenerator.live(alphabet)
      yield gen

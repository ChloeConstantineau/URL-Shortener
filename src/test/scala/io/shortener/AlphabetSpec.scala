package io.shortener

import io.shortener.services.Alphabet
import zio.Scope
import zio.test.*

object AlphabetSpec extends ZIOSpecDefault:

    def spec: Spec[TestEnvironment & Scope, Any] =
      suite("AlphabetSpec")(
        test("Should have default alphabet when 'None' is provided") {
          assertZIO(Alphabet.live(None))(Assertion.equalTo(Alphabet.default))
        },
        test("Should fallback to default when provided alphabet is too small") {
          checkN(1)(Gen.alphaNumericStringBounded(0, Alphabet.minLength - 1)): a =>
            assertZIO(Alphabet.live(Some(a)))(Assertion.equalTo(Alphabet.default))
        },
        test("Should fallback to default when provided alphabet has duplicate characters") {
          val duplicates = (('a' to 'z') ++ ('a' to 'z')).toString()
          assertZIO(Alphabet.live(Some(duplicates)))(Assertion.equalTo(Alphabet.default))
        },
        test("Should fallback to default when provided alphabet has multibyte characters") {
          checkN(1)(Gen.stringBounded(Alphabet.minLength, 50)(Gen.unicodeChar)): a =>
            assertZIO(Alphabet.live(Some(a)))(Assertion.equalTo(Alphabet.default))
        },
        test("Should succeed when valid alphabet value is provided") {
          check(Gen.setOfBounded(Alphabet.minLength, 30)(Gen.asciiChar)): set =>
              val a = set.foldLeft("")((acc, i) => acc + i.toString)
              assertZIO(Alphabet.live(Some(a)))(Assertion.not(Assertion.equalTo(Alphabet.default)))
        },
      ) @@ TestAspect.silentLogging

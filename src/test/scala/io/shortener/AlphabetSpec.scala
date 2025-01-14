package io.shortener

import io.shortener.slug.Alphabet
import zio.Scope
import zio.test.*

object AlphabetSpec extends ZIOSpecDefault:

    def spec: Spec[TestEnvironment & Scope, Any] =
      suite("AlphabetSpec")(
        test("Should fallback to default when no alphabet is provided") {
          val someAlphabet = Alphabet.live(None)
          assertZIO(someAlphabet)(equalsFallback)
        },
        test("Should fallback to default when provided alphabet is too small") {
          check(Gen.alphaNumericStringBounded(0, Alphabet.MIN_LENGTH - 1)): a =>
              val someAlphabet = Alphabet.live(Some(a))
              assertZIO(someAlphabet)(equalsFallback)
        },
        test("Should fallback to default when provided alphabet has duplicate characters") {
          val duplicates   = (('a' to 'z') ++ ('a' to 'z')).toString()
          val someAlphabet = Alphabet.live(Some(duplicates))
          assertZIO(someAlphabet)(equalsFallback)
        },
        test("Should fallback to default when provided alphabet has multibyte characters") {
          check(Gen.stringBounded(Alphabet.MIN_LENGTH, 50)(Gen.unicodeChar)): a =>
              val someAlphabet = Alphabet.live(Some(a))
              assertZIO(someAlphabet)(equalsFallback)
        },
        test("Should succeed when valid alphabet value is provided") {
          check(Gen.setOfBounded(Alphabet.MIN_LENGTH, 30)(Gen.asciiChar)): set =>
              val someAlphabet = Alphabet.live(Some(set.foldLeft("")((acc, i) => acc + i.toString)))
              assertZIO(someAlphabet)(notEqualToFallback)
        },
        test("Alphabet shuffle should be deterministic") {
          assertTrue(alphabet.shuffle == alphabet.shuffle)
        },
        test("Should validate ids") {
          val id = "aAbBcC"
          assertTrue(alphabet.validId(id), !alphabet.validId(id + "invalid id!"))
        },
        test("Should generate valid ids") {
          check(Gen.long): i =>
              assertTrue(alphabet.validId(alphabet.toId(i)))
        },
      ) @@ TestAspect.silentLogging

    private val alphabet: Alphabet = Alphabet.default
    private val equalsFallback     = Assertion.equalTo(alphabet)
    private val notEqualToFallback = Assertion.not(equalsFallback)

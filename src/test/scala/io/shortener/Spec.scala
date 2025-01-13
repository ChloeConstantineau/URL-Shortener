package io.shortener

import io.shortener.Spec.{suite, test}
import zio.*
import zio.test.*
import zio.test.Assertion.*

import java.io.IOException

object Spec extends ZIOSpecDefault {
  def spec: Spec[Any, IOException] = suite("Spec")(
    test("Hello World") {
      for {
        _      <- Console.printLine("Hello, World!")
        output <- TestConsole.output
      } yield assertTrue(output == Vector("Hello, World!\n"))
    }
  )
}
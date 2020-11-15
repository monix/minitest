package minitest.macros

import java.util.regex.Pattern

import scala.compiletime.testing._

import minitest.api.{AssertionException, SourceLocation}

object CompileMacros {
  inline def doesNotCompile(inline code: String, expected: Option[String], pos: SourceLocation): Unit = {
      val errors = typeCheckErrors(code)
      if (errors.isEmpty)
          throw new AssertionException("Type-checking succeeded unexpectedly", pos)
      else
        expected match {
          case Some(expected) if !errors.exists(error => error.message.matches(expected)) =>
            throw new AssertionException("Wrong compile error", pos)

          case _ =>
            ()
        }
  }
}

trait CompileMacros {
  inline def assertDoesNotCompile(inline code: String)(implicit pos: SourceLocation): Unit =
    CompileMacros.doesNotCompile(code, Option.empty, pos)

  inline def assertDoesNotCompile(inline code: String, expected: String)(implicit pos: SourceLocation): Unit =
    CompileMacros.doesNotCompile(code, Some(expected), pos)
}

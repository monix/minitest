package minitest.macros

import java.util.regex.Pattern

import scala.reflect.macros.{ whitebox, ParseException, TypecheckException }
import scala.language.experimental.macros

private object CompileMacros {
  /**
    * Shamelessly copied from Shapeless, copyright by Miles Sabin.
    */
  class DoesNotCompileMacros(val c: whitebox.Context) {
    import c.universe._

    def applyImplNoExp(code: Tree): Tree = applyImpl(code, null)

    def applyImpl(code: Tree, expected: Tree): Tree = {
      val Literal(Constant(codeStr: String)) = code
      val (expPat, expMsg) = expected match {
        case null => (null, "Expected some error.")
        case Literal(Constant(s: String)) =>
          (Pattern.compile(s, Pattern.CASE_INSENSITIVE | Pattern.DOTALL), "Expected error matching: "+s)
      }

      try {
        val dummy0 = TermName(c.freshName)
        val dummy1 = TermName(c.freshName)
        c.typecheck(c.parse(s"object $dummy0 { val $dummy1 = { $codeStr } }"))
        c.error(c.enclosingPosition, "Type-checking succeeded unexpectedly.\n"+expMsg)
      } catch {
        case e: TypecheckException =>
          val msg = e.getMessage
          if((expected ne null) && !(expPat.matcher(msg)).matches)
            c.error(c.enclosingPosition, "Type-checking failed in an unexpected way.\n"+expMsg+"\nActual error: "+msg)
        case e: ParseException =>
          c.error(c.enclosingPosition, s"Parsing failed.\n${e.getMessage}")
      }

      q"()"
    }
  }
}

private[minitest] trait CompileMacros {
  def assertDoesNotCompile(code: String): Unit =
    macro CompileMacros.DoesNotCompileMacros.applyImplNoExp

  def assertDoesNotCompile(code: String, expected: String): Unit =
    macro CompileMacros.DoesNotCompileMacros.applyImpl
}

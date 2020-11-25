package minitest.macros

import scala.quoted._

import minitest.api.SourceLocation

object SourceLocationMacros {
  def impl()(using Quotes): Expr[SourceLocation] = {
    import quotes.reflect._
    val pos = Position.ofMacroExpansion
    val path = pos.sourceFile.jpath
    val startLine = pos.startLine + 1
    '{
      SourceLocation(
        ${Expr(Some(path.getFileName.toString))},
        ${Expr(Some(path.toString))},
        ${Expr(startLine)}
      )
    }
  }
}

trait SourceLocationMacros {
  inline implicit def fromContext: SourceLocation =
    ${ SourceLocationMacros.impl() }
}

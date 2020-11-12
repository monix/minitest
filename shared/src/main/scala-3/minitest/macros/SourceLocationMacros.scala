package minitest.macros

import scala.quoted._

import minitest.api.SourceLocation

private object SourceLocationMacros {
  def impl()(using ctx: QuoteContext): Expr[SourceLocation] = {
    import qctx.tasty._
    val path = rootPosition.sourceFile.jpath
    val startLine = rootPosition.startLine + 1
    '{
      SourceLocation(
        ${Expr(Some(path.getFileName.toString))},
        ${Expr(Some(path.toString))},
        ${Expr(startLine)}
      )
    }
  }
}

private[minitest] trait SourceLocationMacros {
  inline implicit def fromContext: SourceLocation =
    ${ SourceLocationMacros.impl() }
}

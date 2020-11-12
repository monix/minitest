package minitest.macros

import scala.language.experimental.macros
import scala.reflect.macros.whitebox
import scala.util.{Try => STry}

import minitest.api.SourceLocation

private object SourceLocationMacros {
  class Macros(val c: whitebox.Context) {
    import c.universe._

    def fromContext: Tree = {
      val (fileNameExpr, pathExpr, lineExpr) = getSourceLocation
      val SourceLocationSym = symbolOf[SourceLocation].companion
      q"""$SourceLocationSym($fileNameExpr, $pathExpr, $lineExpr)"""
    }

    private def getSourceLocation = {
      val line = c.Expr[Int](Literal(Constant(c.enclosingPosition.line)))
      val file = STry(Option(c.enclosingPosition.source.file.file)).toOption.flatten
      (wrapOption(file.map(_.getName)), wrapOption(file.map(_.getPath)), line)
    }

    private def wrapOption[A](opt: Option[A]): c.Expr[Option[A]] =
      c.Expr[Option[A]](
        opt match {
          case None =>
            q"""_root_.scala.None"""
          case Some(value) =>
            val v = c.Expr[A](Literal(Constant(value)))
            q"""_root_.scala.Option($v)"""
        })
  }
}

private[minitest] trait SourceLocationMacros {
  implicit def fromContext: SourceLocation =
    macro SourceLocationMacros.Macros.fromContext
}

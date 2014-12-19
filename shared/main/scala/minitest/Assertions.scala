package minitest

import minitest.api.{ExpectationException, NotOurException, UnexpectedException}

import scala.language.experimental.macros
import scala.reflect.macros.blackbox
import scala.util.control.NonFatal

trait Assertions {
  def assert(condition: Boolean): Unit =
    macro Assertions.Macros.assert

  def assert(condition: Boolean, hint: String): Unit =
    macro Assertions.Macros.assertWithHint

  def intercept[E <: Throwable](callback: Unit) =
    macro Assertions.Macros.intercept[E]
}

object Assertions extends Assertions {
  object Macros {
    def assert(c: blackbox.Context)(condition: c.Expr[Boolean]): c.Expr[Unit] = {
      import c.universe._

      val (pathExpr, lineExpr) = location(c)
      val conditionRepr: c.Expr[String] = {
        val paramRep = showCode(condition.tree)
        val paramRepTree = Literal(Constant(paramRep))
        c.Expr[String](paramRepTree)
      }

      reify {
        if (!condition.splice)
          throw new ExpectationException(
            "assertion failed: " + conditionRepr.splice,
            pathExpr.splice, lineExpr.splice)
      }
    }

    def assertWithHint(c: blackbox.Context)(condition: c.Expr[Boolean], hint: c.Expr[String]): c.Expr[Unit] = {
      import c.universe._
      val (pathExpr, lineExpr) = location(c)

      reify {
        val path = pathExpr.splice
        val line = lineExpr.splice

        try if (!condition.splice) {
          throw new ExpectationException(hint.splice, path, line)
        }
        catch {
          case NotOurException(ex) =>
            throw new UnexpectedException(ex, path, line)
        }
      }
    }

    def intercept[E <: Throwable : c.WeakTypeTag](c: blackbox.Context)(callback: c.Expr[Unit]): c.Expr[Unit] = {
      import c.universe._
      val (pathExpr, lineExpr) = location(c)

      val typeTag = weakTypeTag[E]
      val nameExpr = c.Expr[String](Literal(Constant(typeTag.tpe.toString)))

      reify {
        val path = pathExpr.splice
        val line = lineExpr.splice
        val name = nameExpr.splice

        try {
          callback.splice
          throw new ExpectationException(s"expected a $name to be thrown", path, line)
        }
        catch {
          case NonFatal(ex) if ex.isInstanceOf[E] =>
            ()
          case NotOurException(ex) =>
            throw new UnexpectedException(ex, path, line)
        }
      }
    }

    def location(c: blackbox.Context) = {
      import c.universe._
      val line = c.Expr[Int](Literal(Constant(c.enclosingPosition.line)))
      val fileName = c.enclosingPosition.source.file.file.getName
      val path = c.Expr[String](Literal(Constant(fileName)))
      (path, line)
    }
  }
}

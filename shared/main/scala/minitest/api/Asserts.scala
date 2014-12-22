package minitest.api

import scala.annotation.tailrec
import scala.language.experimental.macros
import scala.reflect.macros.blackbox
import scala.util.control.NonFatal

trait Asserts {
  def assert(condition: Boolean): Unit =
    macro Asserts.Macros.assert

  def assert(condition: Boolean, hint: String): Unit =
    macro Asserts.Macros.assertWithHint

  def assertResult[T](expected: T)(callback: T): Unit =
    macro Asserts.Macros.assertResult[T]

  def assertResult[T](expected: T, hint: String)(callback: T): Unit =
    macro Asserts.Macros.assertResultWithHint[T]

  def assertEquals[T](received: T, expected: T): Unit =
    macro Asserts.Macros.assertEquals[T]

  def intercept[E <: Throwable](callback: Unit): Unit =
    macro Asserts.Macros.intercept[E]

  def cancel(): Unit =
    throw new CanceledException(None, None)

  def cancel(reason: String): Unit =
    macro Asserts.Macros.cancel

  def ignore(): Unit =
    throw new IgnoredException(None, None)

  def ignore(reason: String): Unit =
    macro Asserts.Macros.ignore

  def fail(): Unit =
    macro Asserts.Macros.fail

  def fail(reason: String): Unit =
    macro Asserts.Macros.failWithReason
}

object Asserts extends Asserts {
  object Macros {
    def cancel(c: blackbox.Context)(reason: c.Expr[String]): c.Expr[Unit] = {
      import c.universe._
      val (pathExpr, lineExpr) = location(c)

      reify {
        if (1 == 1) throw new CanceledException(
          Some(reason.splice),
          Some(SourceLocation(pathExpr.splice, lineExpr.splice))
        )
      }
    }

    def ignore(c: blackbox.Context)(reason: c.Expr[String]): c.Expr[Unit] = {
      import c.universe._
      val (pathExpr, lineExpr) = location(c)

      reify {
        if (1 == 1) throw new IgnoredException(
          Some(reason.splice),
          Some(SourceLocation(pathExpr.splice, lineExpr.splice))
        )
      }
    }

    def fail(c: blackbox.Context)(): c.Expr[Unit] = {
      import c.universe._
      val (pathExpr, lineExpr) = location(c)

      reify {
        if (1 == 1) throw new AssertionException(
          "failed",
          SourceLocation(pathExpr.splice, lineExpr.splice)
        )
      }
    }

    def failWithReason(c: blackbox.Context)(reason: c.Expr[String]): c.Expr[Unit] = {
      import c.universe._
      val (pathExpr, lineExpr) = location(c)

      reify {
        if (1 == 1) throw new AssertionException(
          reason.splice,
          SourceLocation(pathExpr.splice, lineExpr.splice)
        )
      }
    }

    def assertEquals[T : c.WeakTypeTag](c: blackbox.Context)
        (received: c.Expr[T], expected: c.Expr[T]): c.Expr[Unit] = {

      import c.universe._
      val (pathExpr, lineExpr) = location(c)

      reify {
        val location = SourceLocation(pathExpr.splice, lineExpr.splice)
        try {
          val r = received.splice
          val e = expected.splice

          if (r != e)
            throw new AssertionException(
              format("expected {0} != received {1}", e, r),
              location
            )
        }
        catch {
          case NotOurException(ex) =>
            throw new UnexpectedException(ex, location)
        }
      }
    }

    def assertResult[T : c.WeakTypeTag](c: blackbox.Context)
        (expected: c.Expr[T])(callback: c.Expr[T]): c.Expr[Unit] = {

      import c.universe._
      val (pathExpr, lineExpr) = location(c)

      reify {
        val location = SourceLocation(pathExpr.splice, lineExpr.splice)
        try {
          val received = callback.splice
          val test = expected.splice

          if (test != received)
            throw new AssertionException(
              format("expected {0}, but got {1}", test, received),
              location
            )
        }
        catch {
          case NotOurException(ex) =>
            throw new UnexpectedException(ex, location)
        }
      }
    }

    def assertResultWithHint[T : c.WeakTypeTag](c: blackbox.Context)
        (expected: c.Expr[T], hint: c.Expr[String])(callback: c.Expr[T]): c.Expr[Unit] = {

      import c.universe._
      val (pathExpr, lineExpr) = location(c)

      reify {
        val location = SourceLocation(pathExpr.splice, lineExpr.splice)
        try {
          val received = callback.splice
          val test = expected.splice

          if (test != received)
            throw new AssertionException(
              format(hint.splice, test, received),
              location
            )
        }
        catch {
          case NotOurException(ex) =>
            throw new UnexpectedException(ex, location)
        }
      }
    }

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
          throw new AssertionException(
            "assertion failed: " + conditionRepr.splice,
            SourceLocation(pathExpr.splice, lineExpr.splice))
      }
    }

    def assertWithHint(c: blackbox.Context)(condition: c.Expr[Boolean], hint: c.Expr[String]): c.Expr[Unit] = {
      import c.universe._
      val (pathExpr, lineExpr) = location(c)

      reify {
        val path = pathExpr.splice
        val line = lineExpr.splice

        try if (!condition.splice) {
          throw new AssertionException(hint.splice,
            SourceLocation(path, line))
        }
        catch {
          case NotOurException(ex) =>
            throw new UnexpectedException(ex,
              SourceLocation(path, line))
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
          throw new AssertionException(s"expected a $name to be thrown",
            SourceLocation(path, line))
        }
        catch {
          case NonFatal(ex) if ex.isInstanceOf[E] =>
            ()
          case NotOurException(ex) =>
            throw new UnexpectedException(ex,
              SourceLocation(path, line))
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

    def format(tpl: String, values: Any*) = {
      @tailrec
      def loop(index: Int, acc: String): String =
        if (index >= values.length) acc else {
          val value = values(index).toString
          val newStr = acc.replaceAll(s"[{]$index[}]", value)
          loop(index + 1, newStr)
        }

      loop(0, tpl)
    }
  }
}

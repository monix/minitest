package minitest.api

import language.experimental.macros
import scala.reflect.macros.blackbox
import scala.util.control.NonFatal

final class Expectation[T](val callback: () => T, val valueName: String)

object Expectation {
  implicit class Extensions[T](val self: Expectation[T]) extends AnyVal {
    def toBe(expected: T): Unit = macro Macros.toEqual[T]
    def toEqual(expected: T): Unit = macro Macros.toEqual[T]

    def toNotBe(notExpected: T): Unit = macro Macros.toNotEqual[T]
    def toNotEqual(notExpected: T): Unit = macro Macros.toNotEqual[T]

    def toBeTrue(implicit ev: T <:< Boolean): Unit = macro Macros.toBeTrue[T]
    def toBeFalse(implicit ev: T <:< Boolean): Unit = macro Macros.toBeFalse[T]

    def toThrow[E <: Throwable]: Unit = macro Macros.toThrow[E]

    def toBeInstanceOf[I]: Unit = macro Macros.toBeInstanceOf[I]
    def toNotBeInstanceOf[I]: Unit = macro Macros.toNotBeInstanceOf[I]

    def toBeNull: Unit = macro Macros.toBeNull
    def toNotBeNull: Unit = macro Macros.toNotBeNull
  }

  object Macros {
    def toEqual[T : c.WeakTypeTag](c: blackbox.Context { type PrefixType = Extensions[T] })
        (expected: c.Expr[T]): c.Expr[Unit] = {

      import c.universe._
      val (pathExpr, lineExpr) = location(c)

      reify {
        val path = pathExpr.splice
        val line = lineExpr.splice

        val (got, value, hint) = try {
          val self = c.prefix.splice.self
          (self.callback(), expected.splice, self.valueName)
        }
        catch {
          case NotOurException(ex) =>
            throw new UnexpectedException(ex, path, line)
        }

        if (value != got) {
          val msg = if (hint.isEmpty) s"expected $value != $got" else s"expected $value for $hint != $got"
          throw new ExpectationException(msg, path, line)
        }
      }
    }

    def toNotEqual[T : c.WeakTypeTag](c: blackbox.Context { type PrefixType = Extensions[T] })
        (notExpected: c.Expr[T]): c.Expr[Unit] = {

      import c.universe._
      val (pathExpr, lineExpr) = location(c)

      reify {
        val path = pathExpr.splice
        val line = lineExpr.splice

        val (got, value, hint) = try {
          val self = c.prefix.splice.self
          (self.callback(), notExpected.splice, self.valueName)
        }
        catch {
          case NotOurException(ex) =>
            throw new UnexpectedException(ex, path, line)
        }

        if (value == got) {
          val msg = if (hint.isEmpty) s"unexpected $value" else s"unexpected $value for $hint"
          throw new ExpectationException(msg, path, line)
        }
      }
    }

    def toBeTrue[T : c.WeakTypeTag](c: blackbox.Context { type PrefixType = Extensions[T] })
        (ev: c.Expr[<:<[T,Boolean]]): c.Expr[Unit] = {

      import c.universe._
      val (pathExpr, lineExpr) = location(c)

      reify {
        val path = pathExpr.splice
        val line = lineExpr.splice

        val (received, hint) = try {
          val self = c.prefix.splice.self
          val value = self.callback().asInstanceOf[Boolean]
          (value, self.valueName)
        }
        catch {
          case NotOurException(ex) =>
            throw new UnexpectedException(ex, path, line)
        }

        if (!received) {
          val msg = (if (hint.isEmpty) "value" else hint) + " is false"
          throw new ExpectationException(msg, path, line)
        }
      }
    }

    def toBeFalse[T : c.WeakTypeTag](c: blackbox.Context { type PrefixType = Extensions[T] })
        (ev: c.Expr[<:<[T,Boolean]]): c.Expr[Unit] = {

      import c.universe._
      val (pathExpr, lineExpr) = location(c)

      reify {
        val path = pathExpr.splice
        val line = lineExpr.splice

        val (received, hint) = try {
          val self = c.prefix.splice.self
          val value = self.callback().asInstanceOf[Boolean]
          (value, self.valueName)
        }
        catch {
          case NotOurException(ex) =>
            throw new UnexpectedException(ex, path, line)
        }

        if (received) {
          val msg = (if (hint.isEmpty) "value" else hint) + " is true"
          throw new ExpectationException(msg, path, line)
        }
      }
    }

    def toThrow[E <: Throwable : c.WeakTypeTag]
      (c: blackbox.Context { type PrefixType = Extensions[_] }): c.Expr[Unit] = {

      import c.universe._
      val (pathExpr, lineExpr) = location(c)
      val typeTag = weakTypeTag[E]
      val nameExpr = c.Expr[String](Literal(Constant(typeTag.tpe.toString)))

      reify {
        val path = pathExpr.splice
        val line = lineExpr.splice
        val self = c.prefix.splice.self
        val name = nameExpr.splice

        try {
          self.callback()
          val hint = self.valueName
          val msg = (if (hint.isEmpty) "" else hint + " ") + " didn't throw a " + name
          throw new ExpectationException(msg, path, line)
        }
        catch {
          case NonFatal(ex) if ex.isInstanceOf[E] =>
            ()
          case NotOurException(ex) =>
            throw new UnexpectedException(ex, path, line)
        }
      }
    }

    def toBeNull(c: blackbox.Context { type PrefixType = Extensions[_] }): c.Expr[Unit] = {
      import c.universe._
      val (pathExpr, lineExpr) = location(c)

      reify {
        val path = pathExpr.splice
        val line = lineExpr.splice
        val self = c.prefix.splice.self

        try {
          val value = self.callback()
          val hint = self.valueName
          if (value != null && value != None) {
            val m = (if (hint.isEmpty) "value " else hint + " ") + "is not null"
            throw new ExpectationException(m, path, line)
          }
        }
        catch {
          case NotOurException(ex) =>
            throw new UnexpectedException(ex, path, line)
        }
      }
    }

    def toNotBeNull(c: blackbox.Context { type PrefixType = Extensions[_] }): c.Expr[Unit] = {
      import c.universe._
      val (pathExpr, lineExpr) = location(c)

      reify {
        val path = pathExpr.splice
        val line = lineExpr.splice
        val self = c.prefix.splice.self

        try {
          val value = self.callback()
          val hint = self.valueName
          if (value == null || value == None) {
            val m = (if (hint.isEmpty) "value " else hint + " ") + "is null"
            throw new ExpectationException(m, path, line)
          }
        }
        catch {
          case NotOurException(ex) =>
            throw new UnexpectedException(ex, path, line)
        }
      }
    }

    def toBeInstanceOf[I : c.WeakTypeTag]
      (c: blackbox.Context { type PrefixType = Extensions[_] }): c.Expr[Unit] = {

      import c.universe._
      val (pathExpr, lineExpr) = location(c)
      val typeTag = weakTypeTag[I]
      val nameExpr = c.Expr[String](Literal(Constant(typeTag.tpe.toString)))

      reify {
        val path = pathExpr.splice
        val line = lineExpr.splice
        val self = c.prefix.splice.self
        val name = nameExpr.splice

        try {
          if (!self.callback().isInstanceOf[I]) {
            val hint = self.valueName
            val m = (if (hint.isEmpty) "object " else hint + " ") + "isn't an instance of " + name
            throw new ExpectationException(m, path, line)
          }
        }
        catch {
          case NotOurException(ex) =>
            throw new UnexpectedException(ex, path, line)
        }
      }
    }

    def toNotBeInstanceOf[I : c.WeakTypeTag]
      (c: blackbox.Context { type PrefixType = Extensions[_] }): c.Expr[Unit] = {

      import c.universe._
      val (pathExpr, lineExpr) = location(c)
      val typeTag = weakTypeTag[I]
      val nameExpr = c.Expr[String](Literal(Constant(typeTag.tpe.toString)))

      reify {
        val path = pathExpr.splice
        val line = lineExpr.splice
        val self = c.prefix.splice.self
        val name = nameExpr.splice

        try {
          if (self.callback().isInstanceOf[I]) {
            val hint = self.valueName
            val m = (if (hint.isEmpty) "object " else hint + " ") + "is an instance of " + name
            throw new ExpectationException(m, path, line)
          }
        }
        catch {
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

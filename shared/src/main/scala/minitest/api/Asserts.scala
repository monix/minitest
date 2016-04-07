/*
 * Copyright (c) 2014-2016 by Alexandru Nedelcu.
 * Some rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package minitest.api

import java.util.regex.Matcher
import minitest.api.compat._
import scala.annotation.tailrec
import scala.language.experimental.macros
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
    def cancel(c: Context)(reason: c.Expr[String]): c.Expr[Unit] = {
      import c.universe._
      val (pathExpr, lineExpr) = location(c)

      reify {
        if (1 == 1) throw new CanceledException(
          Some(reason.splice),
          Some(SourceLocation(pathExpr.splice, lineExpr.splice))
        )
      }
    }

    def ignore(c: Context)(reason: c.Expr[String]): c.Expr[Unit] = {
      import c.universe._
      val (pathExpr, lineExpr) = location(c)

      reify {
        if (1 == 1) throw new IgnoredException(
          Some(reason.splice),
          Some(SourceLocation(pathExpr.splice, lineExpr.splice))
        )
      }
    }

    def fail(c: Context)(): c.Expr[Unit] = {
      import c.universe._
      val (pathExpr, lineExpr) = location(c)

      reify {
        if (1 == 1) throw new AssertionException(
          "failed",
          SourceLocation(pathExpr.splice, lineExpr.splice)
        )
      }
    }

    def failWithReason(c: Context)(reason: c.Expr[String]): c.Expr[Unit] = {
      import c.universe._
      val (pathExpr, lineExpr) = location(c)

      reify {
        if (1 == 1) throw new AssertionException(
          reason.splice,
          SourceLocation(pathExpr.splice, lineExpr.splice)
        )
      }
    }

    def assertEquals[T : c.WeakTypeTag](c: Context)
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

    def assertResult[T : c.WeakTypeTag](c: Context)
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

    def assertResultWithHint[T : c.WeakTypeTag](c: Context)
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

    def assert(c: Context)(condition: c.Expr[Boolean]): c.Expr[Unit] = {
      import c.universe._

      val (pathExpr, lineExpr) = location(c)
      reify {
        if (!condition.splice)
          throw new AssertionException(
            "assertion failed",
            SourceLocation(pathExpr.splice, lineExpr.splice))
      }
    }

    def assertWithHint(c: Context)(condition: c.Expr[Boolean], hint: c.Expr[String]): c.Expr[Unit] = {
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

    def intercept[E <: Throwable : c.WeakTypeTag](c: Context)(callback: c.Expr[Unit]): c.Expr[Unit] = {
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
          throw new InterceptException(s"expected a $name to be thrown", SourceLocation(path, line))
        }
        catch {
          case ex: InterceptException =>
            throw new AssertionException(ex.message, ex.location)
          case NonFatal(ex) if ex.isInstanceOf[E] =>
            ()
          case NotOurException(ex) =>
            throw new UnexpectedException(ex, SourceLocation(path, line))
        }
      }
    }

    def location(c: Context): (c.Expr[String], c.Expr[Int]) = {
      import c.universe._
      val line = c.Expr[Int](Literal(Constant(c.enclosingPosition.line)))
      val fileName = c.enclosingPosition.source.file.file.getName
      val path = c.Expr[String](Literal(Constant(fileName)))
      (path, line)
    }

    def format(tpl: String, values: Any*): String = {
      @tailrec
      def loop(index: Int, acc: String): String =
        if (index >= values.length) acc else {
          val value = String.valueOf(values(index))
          val newStr = acc.replaceAll(s"[{]$index[}]", Matcher.quoteReplacement(value))
          loop(index + 1, newStr)
        }

      loop(0, tpl)
    }
  }
}

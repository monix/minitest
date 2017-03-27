/*
 * Copyright (c) 2014-2017 by its authors. Some rights reserved.
 * See the project homepage at: https://github.com/monix/minitest
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
import scala.reflect.macros.whitebox

trait Asserts {
  def assert(condition: Boolean): Unit =
    macro Asserts.Macros.assert

  def assert(condition: Boolean, hint: String): Unit =
    macro Asserts.Macros.assertWithHint

  def assertResult[T](expected: T)(callback: T): Unit =
    macro Asserts.Macros.assertResult

  def assertResult[T](expected: T, hint: String)(callback: T): Unit =
    macro Asserts.Macros.assertResultWithHint

  def assertEquals[T](received: T, expected: T): Unit =
    macro Asserts.Macros.assertEquals

  def intercept[E <: Throwable](callback: => Unit): Unit =
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
  @macrocompat.bundle class Macros(val c: whitebox.Context) {
    import c.universe._

    def cancel(reason: Tree): Tree = {
      val (pathExpr, lineExpr) = getSourceLocation

      val SourceLocationSym = symbolOf[SourceLocation].companion
      val CanceledExceptionSym = symbolOf[CanceledException]
      val SomeSym = symbolOf[Some[_]].companion

      q"""
      if (1 == 1) throw new $CanceledExceptionSym(
        $SomeSym($reason),
        $SomeSym($SourceLocationSym($pathExpr, $lineExpr))
      )
      """
    }

    def ignore(reason: Tree): Tree = {
      val (pathExpr, lineExpr) = getSourceLocation

      val SourceLocationSym = symbolOf[SourceLocation].companion
      val IgnoredExceptionSym = symbolOf[IgnoredException]
      val SomeSym = symbolOf[Some[_]].companion

      q"""
      if (1 == 1) throw new $IgnoredExceptionSym(
        $SomeSym($reason),
        $SomeSym($SourceLocationSym($pathExpr, $lineExpr))
      )
      """
    }

    def fail(): Tree = {
      val (pathExpr, lineExpr) = getSourceLocation

      val SourceLocationSym = symbolOf[SourceLocation].companion
      val AssertionExceptionSym = symbolOf[AssertionException]
      val SomeSym = symbolOf[Some[_]].companion

      q"""
      if (1 == 1) throw new $AssertionExceptionSym(
        "failed",
        $SourceLocationSym($pathExpr, $lineExpr)
      )
      """
    }

    def failWithReason(reason: Tree): Tree = {
      val (pathExpr, lineExpr) = getSourceLocation

      val SourceLocationSym = symbolOf[SourceLocation].companion
      val AssertionExceptionSym = symbolOf[AssertionException]
      val SomeSym = symbolOf[Some[_]].companion

      q"""
      if (1 == 1) throw new $AssertionExceptionSym(
        $reason,
        $SourceLocationSym($pathExpr, $lineExpr)
      )
      """
    }

    def assertEquals(received: Tree, expected: Tree): Tree = {
      val (pathExpr, lineExpr) = getSourceLocation

      val SourceLocationSym = symbolOf[SourceLocation].companion
      val AssertionExceptionSym = symbolOf[AssertionException]
      val UnexpectedExceptionSym = symbolOf[UnexpectedException]
      val AssertsSym = symbolOf[Asserts].companion

      val locationSym = freshTermName(c)("location")
      val rs = freshTermName(c)("r")
      val es = freshTermName(c)("e")
      val ex = freshTermName(c)("ex")

      q"""
       val $locationSym = $SourceLocationSym($pathExpr, $lineExpr)
       try {
         val $rs = $received
         val $es = $expected

          if ($rs != $es)
            throw new $AssertionExceptionSym(
              $AssertsSym.format("received {0} != expected {1}", $rs, $es),
              $locationSym
            )
        }
        catch {
          case _root_.minitest.api.NotOurException($ex) =>
            throw new $UnexpectedExceptionSym($ex, $locationSym)
        }
       """
    }

    def assertResult(expected: Tree)(callback: Tree): Tree = {
      val (pathExpr, lineExpr) = getSourceLocation

      val SourceLocationSym = symbolOf[SourceLocation].companion
      val AssertionExceptionSym = symbolOf[AssertionException]
      val UnexpectedExceptionSym = symbolOf[UnexpectedException]
      val AssertsSym = symbolOf[Asserts].companion

      val locationSym = freshTermName(c)("location")
      val rs = freshTermName(c)("r")
      val es = freshTermName(c)("e")
      val ex = freshTermName(c)("ex")

      q"""
       val $locationSym = $SourceLocationSym($pathExpr, $lineExpr)
       try {
         val $rs = $callback
         val $es = $expected

          if ($rs != $es)
            throw new $AssertionExceptionSym(
              $AssertsSym.format("received {0} != expected {1}", $rs, $es),
              $locationSym
            )
        }
        catch {
          case _root_.minitest.api.NotOurException($ex) =>
            throw new $UnexpectedExceptionSym($ex, $locationSym)
        }
       """
    }

    def assertResultWithHint(expected: Tree, hint: Tree)(callback: Tree): Tree = {
      val (pathExpr, lineExpr) = getSourceLocation

      val SourceLocationSym = symbolOf[SourceLocation].companion
      val AssertionExceptionSym = symbolOf[AssertionException]
      val UnexpectedExceptionSym = symbolOf[UnexpectedException]
      val AssertsSym = symbolOf[Asserts].companion

      val locationSym = freshTermName(c)("location")
      val rs = freshTermName(c)("r")
      val es = freshTermName(c)("e")
      val ex = freshTermName(c)("ex")
      val hintStr = freshTermName(c)("hintStr")

      q"""
       val $locationSym = $SourceLocationSym($pathExpr, $lineExpr)
       try {
         val $rs = $callback
         val $es = $expected
         val $hintStr = $hint

         if ($rs != $es)
           throw new $AssertionExceptionSym(
             $AssertsSym.format($hintStr, $rs, $es),
             $locationSym
           )
        }
        catch {
          case _root_.minitest.api.NotOurException($ex) =>
            throw new $UnexpectedExceptionSym($ex, $locationSym)
        }
       """
    }

    def assert(condition: Tree): Tree = {
      val (pathExpr, lineExpr) = getSourceLocation

      val SourceLocationSym = symbolOf[SourceLocation].companion
      val AssertionExceptionSym = symbolOf[AssertionException]
      val UnexpectedExceptionSym = symbolOf[UnexpectedException]

      val isFalse = freshTermName(c)("isFalse")
      val locationSym = freshTermName(c)("location")
      val ex = freshTermName(c)("ex")

      q"""
       val $locationSym = $SourceLocationSym($pathExpr, $lineExpr)
       try {
         val $isFalse = !$condition

         if ($isFalse)
           throw new $AssertionExceptionSym(
             "assertion failed",
             $locationSym
           )
        }
        catch {
          case _root_.minitest.api.NotOurException($ex) =>
            throw new $UnexpectedExceptionSym($ex, $locationSym)
        }
       """
    }

    def assertWithHint(condition: Tree, hint: Tree): Tree = {
      val (pathExpr, lineExpr) = getSourceLocation

      val SourceLocationSym = symbolOf[SourceLocation].companion
      val AssertionExceptionSym = symbolOf[AssertionException]
      val UnexpectedExceptionSym = symbolOf[UnexpectedException]

      val isFalse = freshTermName(c)("isFalse")
      val locationSym = freshTermName(c)("location")
      val ex = freshTermName(c)("ex")
      val hintStr = freshTermName(c)("hint")

      q"""
       val $locationSym = $SourceLocationSym($pathExpr, $lineExpr)
       try {
         val $isFalse = !$condition
         val $hintStr = $hint

         if ($isFalse)
           throw new $AssertionExceptionSym(
             $hintStr,
             $locationSym
           )
        }
        catch {
          case _root_.minitest.api.NotOurException($ex) =>
            throw new $UnexpectedExceptionSym($ex, $locationSym)
        }
       """
    }

    def intercept[E <: Throwable : WeakTypeTag](callback: Tree): Tree = {
      val (pathExpr, lineExpr) = getSourceLocation

      val typeTag = weakTypeTag[E]
      val eSymbol = symbolOf[E]
      val nameExpr = c.Expr[String](Literal(Constant(typeTag.tpe.toString)))
      val InterceptExceptionSym = symbolOf[InterceptException]
      val SourceLocationSym = symbolOf[SourceLocation].companion
      val AssertionExceptionSym = symbolOf[AssertionException]
      val UnexpectedExceptionSym = symbolOf[UnexpectedException]

      val path = freshTermName(c)("path")
      val line = freshTermName(c)("line")
      val name = freshTermName(c)("name")
      val ex = freshTermName(c)("ex")

      q"""
       val $path = $pathExpr
       val $line = $lineExpr
       val $name = $nameExpr

       try {
         $callback
         throw new $InterceptExceptionSym(
           "expected a " + $name.toString + " to be thrown",
           $SourceLocationSym($path, $line)
         )
       }
       catch {
         case $ex: $InterceptExceptionSym =>
           throw new $AssertionExceptionSym($ex.message, $ex.location)
         case _root_.scala.util.control.NonFatal($ex) if $ex.isInstanceOf[$eSymbol] =>
           ()
         case _root_.minitest.api.NotOurException($ex) =>
           throw new $UnexpectedExceptionSym($ex, $SourceLocationSym($path, $line))
       }
       """
    }

    def getSourceLocation: (Expr[String], Expr[Int]) = {
      val line = c.Expr[Int](Literal(Constant(c.enclosingPosition.line)))
      val fileName = c.enclosingPosition.source.file.file.getName
      val path = c.Expr[String](Literal(Constant(fileName)))
      (path, line)
    }
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

/*
 * Copyright (c) 2014-2018 by The Minitest Project Developers.
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

import scala.util.{Try => STry}
import scala.language.experimental.macros
import scala.reflect.macros.whitebox

final case class SourceLocation(
  fileName: Option[String],
  filePath: Option[String],
  line: Int
)

object SourceLocation {
  implicit def fromContext: SourceLocation =
    macro Macros.fromContext

  @macrocompat.bundle class Macros(val c: whitebox.Context) {
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
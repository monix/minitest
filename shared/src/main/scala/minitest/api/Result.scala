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

import scala.compat.Platform.EOL
import scala.Console.{GREEN, RED, YELLOW}

sealed trait Result[+T] {
  def formatted(name: String): String
}

object Result {
  final case class Success[+T](value: T) extends Result[T] {
    def formatted(name: String): String = {
      GREEN + "- " + name + EOL
    }
  }

  final case class Ignored(reason: Option[String], location: Option[SourceLocation])
    extends Result[Nothing] {

    def formatted(name: String): String = {
      val reasonStr = reason.fold("")(msg => formatDescription(msg, location, YELLOW, "  "))
      YELLOW + "- " + name + " !!! IGNORED !!!" + EOL + reasonStr
    }
  }

  final case class Canceled(reason: Option[String], location: Option[SourceLocation])
    extends Result[Nothing] {

    def formatted(name: String): String = {
      val reasonStr = reason.fold("")(msg => formatDescription(msg, location, YELLOW, "  "))
      YELLOW + "- " + name + " !!! CANCELED !!!" + EOL + reasonStr
    }
  }

  final case class Failure(msg: String, source: Option[Throwable], location: Option[SourceLocation])
    extends Result[Nothing] {

    def formatted(name: String): String =
      formatError(name, msg, source, location, Some(20))
  }

  final case class Exception(source: Throwable, location: Option[SourceLocation])
    extends Result[Nothing] {

    def formatted(name: String): String = {
      val description = {
        val name = source.getClass.getName
        val className = name.substring(name.lastIndexOf(".") + 1)
        Option(source.getMessage).filterNot(_.isEmpty)
          .fold(className)(m => s"$className: $m")
      }

      formatError(name, description, Some(source), location, None)
    }
  }

  def from(error: Throwable): Result[Nothing] = error match {
    case ex: AssertionException =>
      Result.Failure(ex.message, Some(ex), Some(ex.location))
    case ex: UnexpectedException =>
      Result.Exception(ex.reason, Some(ex.location))
    case ex: InterceptException =>
      Result.Exception(ex, Some(ex.location))
    case ex: IgnoredException =>
      Result.Ignored(ex.reason, ex.location)
    case ex: CanceledException =>
      Result.Canceled(ex.reason, ex.location)
    case other =>
      Result.Exception(other, None)
  }

  private def formatError(name: String, msg: String,
    source: Option[Throwable],
    location: Option[SourceLocation],
    traceLimit: Option[Int]): String = {

    val stackTrace = source.fold("") { ex =>
      val trace: Array[String] = {
        val tr = ex.getStackTrace.map(_.toString)
        traceLimit.fold(tr) { limit =>
          if (tr.length <= limit) tr else
            tr.take(limit) :+ "..."
        }
      }

      formatDescription(trace.mkString("\n"), None, RED, "    ")
    }

    val formattedMessage = formatDescription(
      if (msg != null && msg.nonEmpty) msg else "Test failed",
      location, RED, "  "
    )

    RED + s"- $name *** FAILED ***" + EOL +
      formattedMessage + stackTrace
  }

  private def formatDescription(
    message: String,
    location: Option[SourceLocation],
    color: String,
    prefix: String): String = {

    val lines = message.split("\\r?\\n").zipWithIndex.map { case (line, index) =>
      if (index == 0)
        color + prefix + line +
          location.fold("")(l => s" (${l.fileName.getOrElse("none")}:${l.line})") +
          EOL
      else
        color + prefix + line + EOL
    }

    lines.mkString
  }
}

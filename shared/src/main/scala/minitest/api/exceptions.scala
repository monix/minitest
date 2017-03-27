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

import scala.util.control.NonFatal

abstract class MiniTestException(message: String, cause: Throwable)
  extends RuntimeException(message, cause)

final class AssertionException(val message: String, val location: SourceLocation)
  extends MiniTestException(message, null)

final class UnexpectedException(val reason: Throwable, val location: SourceLocation)
  extends MiniTestException(null, reason)

final class IgnoredException(val reason: Option[String], val location: Option[SourceLocation])
  extends MiniTestException(reason.orNull, null)

final class CanceledException(val reason: Option[String], val location: Option[SourceLocation])
  extends MiniTestException(reason.orNull, null)

final class InterceptException(val message: String, val location: SourceLocation)
  extends MiniTestException(message, null)

object OurException {
  /**
   * Utility for pattern matching.
   */
  def unapply(ex: Throwable): Option[MiniTestException] = ex match {
    case ref: MiniTestException =>
      Some(ref)
    case _ =>
      None
  }
}

object NotOurException {
  /**
   * Utility for pattern matching.
   */
  def unapply(ex: Throwable) = ex match {
    case OurException(_) =>
      None
    case NonFatal(_) =>
      Some(ex)
    case _ =>
      None
  }
}
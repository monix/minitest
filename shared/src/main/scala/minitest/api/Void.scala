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

import scala.language.implicitConversions

/** Replacement of `Unit`.
  *
  * Scala automatically converts non-`Unit` values into `Unit`,
  * making it impossible to detect when users are actually
  * returning `Unit` or not in their tests.
  *
  * `Void` on the other hand boxes any such value, such
  * that we can detect it in tests and deliver a meaningful
  * error.
  */
sealed abstract class Void

object Void {
  /** Returns the equivalent of a `Unit`. */
  def unit: Void = UnitRef

  /** The result of a `Unit` to `Void` conversion. */
  case object UnitRef extends Void

  /** Represents a reference that was caught by a conversion. */
  final case class Caught[A](ref: A, location: SourceLocation)
    extends Void

  /** Implicit conversion that boxes everything except for `Unit`. */
  implicit def toVoid[A](ref: A)(implicit location: SourceLocation): Void =
    ref match {
      case () => Void.UnitRef
      case _ => Void.Caught(ref, location)
    }
}

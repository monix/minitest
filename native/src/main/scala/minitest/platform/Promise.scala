/*
 * Copyright (c) 2014-2019 by The Minitest Project Developers.
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

package minitest
package platform

import scala.util.{Success, Try}

/**
  * Stub needed because Scala Native does not provide an
  * implementation for [[scala.concurrent.Promise]] yet.
  *
  * Note that this isn't a proper `Future` implementation,
  * just something very simple for compilation to work and
  * to pass the current tests.
  */
final class Promise[A] private (private var value: Option[Try[A]] = None) {
  def success(value: A): this.type = {
    this.value = Some(Success(value))
    this
  }

  def future: Future[A] =
    new Future(value.getOrElse(sys.error("not completed")))
}

object Promise {
  def apply[A](): Promise[A] = new Promise[A]()
}

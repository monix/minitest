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

package minitest.platform

import scala.concurrent.Future
import scala.concurrent.duration.Duration

/**
 * Stub needed because Scala Native's Await is not useful.
 *
 * Note that this isn't a proper `Await` implementation,
 * just something very simple for compilation to work and
 * to pass the current tests.
 */
object Await {
  def result[A](future: Future[A], duration: Duration): A = {
    scala.scalanative.runtime.loop()
    future.value.get.get
  }
}

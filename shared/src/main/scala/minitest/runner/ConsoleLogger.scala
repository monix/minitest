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

package minitest.runner

import sbt.testing.Logger

final class ConsoleLogger extends Logger {
  private[this] val withColors =
    System.getenv().get("TERM") != null

  def ansiCodesSupported(): Boolean =
    withColors
  def error(msg: String): Unit =
    print(msg)
  def warn(msg: String): Unit =
    print(msg)
  def info(msg: String): Unit =
    print(msg)
  def debug(msg: String): Unit =
    print(msg)
  def trace(t: Throwable): Unit =
    t.printStackTrace(System.out)
}

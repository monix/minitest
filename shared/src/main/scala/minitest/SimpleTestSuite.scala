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

package minitest

import minitest.api._

trait SimpleTestSuite extends AbstractTestSuite with Asserts {
  def test[T : TestBuilder](name: String)(f: => T): Unit =
    synchronized {
      if (isInitialized) throw new AssertionError(
        "Cannot define new tests after SimpleTestSuite was initialized")
      propertiesSeq = propertiesSeq :+ implicitly[TestBuilder[T]].build[Unit](name, _ => f)
    }

  lazy val properties: Properties[_] =
    synchronized {
      implicit val ec = DefaultExecutionContext
      if (!isInitialized) isInitialized = true
      Properties[Unit](() => (), _ => (), propertiesSeq)
    }

  private[this] var propertiesSeq = Seq.empty[TestSpec[Unit, Unit]]
  private[this] var isInitialized = false
}

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
  def test(name: String)(f: => Void): Unit =
    synchronized {
      if (isInitialized) throw initError()
      propertiesSeq = propertiesSeq :+ TestSpec.sync[Unit](name, _ => f)
    }

  def testAsync(name: String)(f: => Future[Unit]): Unit =
    synchronized {
      if (isInitialized) throw initError()
      propertiesSeq = propertiesSeq :+ TestSpec.async[Unit](name, _ => f)
    }

  lazy val properties: Properties[_] =
    synchronized {
      if (!isInitialized) isInitialized = true
      Properties[Unit](() => (), _ => Void.UnitRef, () => (), () => (), propertiesSeq)
    }

  private[this] var propertiesSeq = Seq.empty[TestSpec[Unit, Unit]]
  private[this] var isInitialized = false
  private[this] implicit lazy val ec: ExecutionContext =
    DefaultExecutionContext

  private[this] def initError() =
    new AssertionError(
      "Cannot define new tests after SimpleTestSuite was initialized"
    )
}

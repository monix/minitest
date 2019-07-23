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

package minitest.tests

import minitest.SimpleTestSuite
import minitest.api.AssertionException
import scala.concurrent.Future

object SimpleTest extends SimpleTestSuite {
  test("ignored test") {
    ignore()
  }

  test("ignore test with reason") {
    ignore("test was ignored with a message")
  }

  test("canceled test") {
    cancel()
  }

  test("canceled test with reason") {
    cancel("test was canceled with a message")
  }

  test("simple assert") {
    def hello: String = "hello"
    assert(hello == "hello")
  }

  test("assert with hint") {
    def hello: String = "hello"
    assert(hello == "hello", "assertion with hint is failing")
  }

  test("assert result without message") {
    assertResult("hello world") {
      "hello" + " world"
    }
  }

  test("assert result with message") {
    assertResult("hello world", "expecting hello failed ({0} != {1})") {
      "hello" + " world"
    }
  }

  test("assert equals") {
    assertEquals(2, 1 + 1)
  }

  test("assert equals with nulls") {
    val s: String = null

    intercept[AssertionException] {
      assertEquals(s, "dummy")
    }

    intercept[AssertionException] {
      assertEquals("dummy", s)
    }
  }

  test("intercept") {
    class DummyException extends RuntimeException
    def test = 1

    intercept[DummyException] {
      if (test != 2) throw new DummyException
    }
  }

  testAsync("asynchronous test") {
    implicit val ec = minitest.platform.DefaultExecutionContext

    Future(1).map(_+1).map { result =>
      assertEquals(result, 2)
    }
  }

  test("intercept failure") {
    class DummyException extends RuntimeException

    intercept[AssertionException] {
      intercept[DummyException] {
        def hello(x: Int) = x + 1
        if (hello(1) != 2) throw new DummyException
      }
    }
  }

  test("fail()") {
    def x = 1
    intercept[AssertionException] { if (x == 1) fail() }
  }

  test("fail(reason)") {
    def x = 1
    val isSuccess = try {
      if (x == 1) fail("dummy")
      false
    }
    catch {
      case ex: AssertionException =>
        ex.message == "dummy"
    }

    assert(isSuccess)
  }
}

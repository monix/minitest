package minitest.tests

import minitest.SimpleTestSuite
import minitest.api.AssertionException

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
}

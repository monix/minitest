package minitest.tests

import minitest.api.ExpectationException
import minitest.{Assertions, SimpleTestSuite}

object AssertionsTest extends SimpleTestSuite with Assertions {
  test("simple assert") {
    def value: Int = 1
    assert(value == 1)
  }

  test("assert that fails") {
    def value: Int = 1

    intercept[ExpectationException] {
      assert(value == 2)
    }
  }
}

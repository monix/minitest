package minitest.tests

import minitest.SimpleTestSuite

object SimpleTest extends SimpleTestSuite {
  test("should be") {
    expect(1 + 1).toBe(2)
  }

  test("should not be") {
    expect(1 + 1).toNotBe(3)
  }

  test("should be true") {
    expect(1 + 1 == 2).toBeTrue
  }

  test("should be false") {
    expect(1 + 1 == 3).toBeFalse
  }

  test("should throw") {
    class DummyException extends RuntimeException("DUMMY")
    def test(): String = throw new DummyException

    expect(test()).toThrow[DummyException]
  }
}

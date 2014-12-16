package minitest.tests

import minitest.SimpleTestSuite

object SimpleTest extends SimpleTestSuite {
  test("should succeed") {
    expect(1 + 1).toBe(2)
  }

  test("should throw") {
    class DummyException extends RuntimeException("DUMMY")
    def test(): String = throw new DummyException

    expect(test()).toThrow[DummyException]
  }
}

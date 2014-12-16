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

  test("should be null") {
    val s: String = null
    expect(s).toBeNull
  }

  test("shouldn't be null") {
    val s: String = "s"
    expect(s).toNotBeNull
  }

  test("should throw") {
    class DummyException extends RuntimeException("DUMMY")
    def test(): String = throw new DummyException

    expect(test()).toThrow[DummyException]
  }

  test("should be instance of thing") {
    case class Thing(v: Int)
    expect(Thing(1)).toBeInstanceOf[Thing]
  }

  test("should not be an instance of thing") {
    case class Thing(v: Int)
    expect(Thing(1)).toNotBeInstanceOf[String]
  }
}

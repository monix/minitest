package minitest.tests

import minitest.SimpleTestSuite

object DoesNotCompileTest extends SimpleTestSuite {
  test("assertDoesNotCompile(code)") {
    assertDoesNotCompile("1.noSuchMethod")
  }

  test("assertDoesNotCompile(code, expected)") {
    assertDoesNotCompile("1.noSuchMethod", ".*?noSuchMethod is not a member of Int")
  }
}

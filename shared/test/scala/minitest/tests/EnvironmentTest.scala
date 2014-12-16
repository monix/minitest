package minitest.tests

import minitest.TestSuite
import scala.util.Random

object EnvironmentTest extends TestSuite[Int] {
  def setup(): Int = {
    Random.nextInt(100) + 1
  }

  def tearDown(env: Int): Unit = {
    expect(env > 0).toBe(true)
  }

  test("should be") { env =>
    expect(env).toBe(env)
  }

  test("should not be") { env =>
    expect(env).toNotBe(0)
  }

  test("should be true") { env =>
    expect(env == env).toBeTrue
  }

  test("should be false") { env =>
    expect(env == 0).toBeFalse
  }
}

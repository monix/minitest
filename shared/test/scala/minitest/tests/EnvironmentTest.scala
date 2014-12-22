package minitest.tests

import minitest.TestSuite
import scala.util.Random

object EnvironmentTest extends TestSuite[Int] {
  def setup(): Int = {
    Random.nextInt(100) + 1
  }

  def tearDown(env: Int): Unit = {
    assert(env > 0)
  }

  test("simple test") { env =>
    assertEquals(env, env)
  }
}

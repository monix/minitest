package minitest

import minitest.api.Expectation

trait Expectations {
  def expect[T](v: => T, hint: String = ""): Expectation[T] =
    new Expectation(() => v, hint.trim)
}

object Expectations extends Expectations

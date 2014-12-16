package minitest.api

import scala.scalajs.js.annotation.JSExportDescendentObjects

@JSExportDescendentObjects
abstract class TestSuite {
  def properties: Properties[_]

  def expect[T](v: => T, hint: String = ""): Expectation[T] =
    new Expectation(() => v, hint.trim)
}

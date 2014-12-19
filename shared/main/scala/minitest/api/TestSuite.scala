package minitest.api

import scala.scalajs.js.annotation.JSExportDescendentObjects

@JSExportDescendentObjects
trait TestSuite {
  def properties: Properties[_]
}

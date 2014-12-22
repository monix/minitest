package minitest.api

import scala.scalajs.js.annotation.JSExportDescendentObjects

@JSExportDescendentObjects
trait AbstractTestSuite {
  def properties: Properties[_]
}

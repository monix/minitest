package minitest.runner

import org.scalajs.testinterface.TestUtils

object Platform {
  def loadModule[T](name: String, loader: ClassLoader): Option[T] = {
    val instance = TestUtils.loadModule(name, loader)
    Option(instance).map(_.asInstanceOf[T])
  }
}

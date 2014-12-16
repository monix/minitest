package minitest.api

import org.scalajs.testinterface.TestUtils
import scala.util.control.NonFatal

object Utils {
  def silent[T](cb: => T): Unit =
    try { cb; () } catch {
      case NonFatal(_) => ()
    }

  def loadModule[T](name: String, loader: ClassLoader): Option[T] = {
    val instance = TestUtils.loadModule(name, loader)
    Option(instance.asInstanceOf[T])
  }
}

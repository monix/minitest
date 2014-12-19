package minitest.api

import scala.util.control.NonFatal

object Utils {
  def silent[T](cb: => T): Unit =
    try { cb; () } catch {
      case NonFatal(_) => ()
    }
}

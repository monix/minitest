package minitest.api

import scala.util.control.NonFatal

final class ExpectationException(val message: String, val path: String, val line: Int)
  extends RuntimeException(message)

final class UnexpectedException(val reason: Throwable, val path: String, val line: Int)
  extends RuntimeException(reason)

object OurException {
  /**
   * Utility for pattern matching.
   */
  def unapply(ex: Throwable) = ex match {
    case NonFatal(_: ExpectationException | _: UnexpectedException) =>
      Some(ex)
    case _ =>
      None
  }
}

object NotOurException {
  /**
   * Utility for pattern matching.
   */
  def unapply(ex: Throwable) = ex match {
    case OurException(_) =>
      None
    case NonFatal(_) =>
      Some(ex)
    case _ =>
      None
  }
}
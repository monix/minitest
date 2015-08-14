package minitest.api

import scala.util.control.NonFatal

abstract class MiniTestException(message: String, cause: Throwable)
  extends RuntimeException(message, cause)

final class AssertionException(val message: String, val location: SourceLocation)
  extends MiniTestException(message, null)

final class UnexpectedException(val reason: Throwable, val location: SourceLocation)
  extends MiniTestException(null, reason)

final class IgnoredException(val reason: Option[String], val location: Option[SourceLocation])
  extends MiniTestException(reason.orNull, null)

final class CanceledException(val reason: Option[String], val location: Option[SourceLocation])
  extends MiniTestException(reason.orNull, null)

object OurException {
  /**
   * Utility for pattern matching.
   */
  def unapply(ex: Throwable): Option[MiniTestException] = ex match {
    case ref: MiniTestException =>
      Some(ref)
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
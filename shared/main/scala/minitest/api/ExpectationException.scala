package minitest.api

final class ExpectationException(val message: String, val path: String, val line: Int)
  extends RuntimeException(message)

final class UnexpectedException(val raison: Throwable, val path: String, val line: Int)
  extends RuntimeException(raison)
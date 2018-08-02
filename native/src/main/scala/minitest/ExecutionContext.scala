package minitest

trait ExecutionContext

object ExecutionContext {
  val global: ExecutionContext = new ExecutionContext{}

  object Implicits {
    implicit val global: ExecutionContext = ExecutionContext.global
  }
}

package minitest

import scala.util.{Success, Try}

object Promise {
  def apply[A](): Promise[A] = new Promise[A]()
}

final class Promise[A] private (private var value: Option[Try[A]] = None) {

  def success(value: A): this.type = {
    this.value = Some(Success(value))
    this
  }

  def future: Future[A] =
    new Future(value.getOrElse(sys.error("not completed")))

}

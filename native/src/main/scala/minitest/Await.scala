package minitest

import scala.concurrent.duration.Duration

object Await {
  def result[A](future: Future[A], duration: Duration): A =
    future.value.get
}

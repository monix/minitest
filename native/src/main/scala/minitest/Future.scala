package minitest

import scala.util.{Failure, Success, Try}

final class Future[+A] private[minitest] (private[minitest] val value: Try[A]) {

  def map[B](f: A => B)(implicit executor: ExecutionContext): Future[B] =
    new Future(value.map(f))

  def flatMap[B](f: A => Future[B])(implicit executor: ExecutionContext): Future[B] =
    new Future(value.flatMap(f andThen(_.value)))

  def onComplete[U](f: Try[A] => U)(implicit executor: ExecutionContext): Unit =
    f(value)

}

object Future {

  def apply[A](f: => A): Future[A] =
    new Future(Try(f))

  def successful[A](value: A): Future[A] =
    new Future(Success(value))

  def failed[A](e: Throwable): Future[A] =
    new Future(Failure(e))

}

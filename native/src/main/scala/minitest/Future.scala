/*
 * Copyright (c) 2014-2018 by The Minitest Project Developers.
 * Some rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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

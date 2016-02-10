/*
 * Copyright (c) 2014-2016 by Alexandru Nedelcu.
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

package minitest.api

import scala.annotation.implicitNotFound
import scala.concurrent.{ExecutionContext, Promise, Future}
import scala.util.{Failure, Success, Try}
import scala.util.control.NonFatal

case class TestSpec[I, +O](name: String, f: I => Future[Result[O]])
  extends (I => Future[Result[O]]) {

  override def apply(v1: I): Future[Result[O]] = f(v1)
}

object TestSpec {
  def async[Env,T](name: String, cb: Env => Future[T])
    (implicit ec: ExecutionContext): TestSpec[Env, Unit] =
    TestSpec[Env, Unit](name,  { env =>
      val p = Promise[Try[T]]()
      val f = try {
        cb(env).onComplete(p.success)
        p.future
      } catch {
        case NonFatal(ex) =>
          Future.failed(ex)
      }

      f.map {
        case Success(_) => Result.Success(())
        case Failure(ex) => Result.from(ex)
      }
    })

  def from[Env,T](name: String, cb: Env => T)
    (implicit ec: ExecutionContext): TestSpec[Env, Unit] =
    TestSpec(name, { env =>
      try {
        cb(env)
        Future.successful(Result.Success(()))
      }
      catch {
        case NonFatal(ex) =>
          Future.successful(Result.from(ex))
      }
    })
}

@implicitNotFound(
  "Test must return either Unit or Future[Unit] and not ${T}. In case of " +
  "Future[Unit] make sure you have an implicit ExecutionContext in scope.")
trait TestBuilder[-T] {
  def build[Env](name: String, f: Env => T): TestSpec[Env, Unit]
}

object TestBuilder {
  private[this] implicit val ec = DefaultExecutionContext

  implicit val synchronous: TestBuilder[Unit] =
    new TestBuilder[Unit] {
      def build[Env](name: String, f: (Env) => Unit): TestSpec[Env, Unit] =
        TestSpec.from(name, f)
    }

  implicit val asynchronous: TestBuilder[Future[Unit]] =
    new TestBuilder[Future[Unit]] {
      def build[Env](name: String, cb: (Env) => Future[Unit]): TestSpec[Env, Unit] =
        TestSpec.async(name, cb)
    }
}



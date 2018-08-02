/*
 * Copyright (c) 2014-2017 by its authors. Some rights reserved.
 * See the project homepage at: https://github.com/monix/minitest
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

import minitest.{ExecutionContext, Future, Promise}
import scala.util.control.NonFatal
import scala.util.{Failure, Success}

case class TestSpec[I, +O](name: String, f: I => Future[Result[O]])
  extends (I => Future[Result[O]]) {

  override def apply(v1: I): Future[Result[O]] = f(v1)
}

object TestSpec {
  def async[Env](name: String, cb: Env => Future[Unit])
    (implicit ec: ExecutionContext): TestSpec[Env, Unit] =
    TestSpec(name,  { env =>
      val f: Future[Unit] =
        try cb(env)
        catch { case NonFatal(ex) => Future.failed(ex) }

      val p = Promise[Result[Unit]]()
      f.onComplete {
        case Success(_) =>
          p.success(Result.Success(()))
        case Failure(ex) =>
          p.success(Result.from(ex))
      }
      p.future
    })

  def sync[Env](name: String, cb: Env => Void): TestSpec[Env, Unit] =
    TestSpec(name, { env =>
      try {
        cb(env) match {
          case Void.UnitRef =>
            Future.successful(Result.Success(()))
          case Void.Caught(ref, loc) =>
            Future.successful(unexpected(ref, loc))
        }
      }
      catch {
        case NonFatal(ex) =>
          Future.successful(Result.from(ex))
      }
    })

  private def unexpected[A](ref: A, loc: SourceLocation): Result[Nothing] =
    Result.Failure(
      s"Problem with test spec, expecting `Unit`, but received: $ref ",
      None, Some(loc)
    )
}

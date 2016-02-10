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

import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NonFatal
import minitest.api.Utils.silent

case class Properties[I](
  setup: () => I,
  tearDown: I => Unit,
  properties: Seq[TestSpec[I, Unit]])
  (implicit ec: ExecutionContext)
  extends Iterable[TestSpec[Unit, Unit]] {

  def iterator: Iterator[TestSpec[Unit, Unit]] = {
    implicit val ec = DefaultExecutionContext

    for (property <- properties.iterator) yield
      TestSpec[Unit, Unit](property.name, { ignore =>
        try {
          val env = setup()
          val result = try property(env) catch {
            case NonFatal(ex) =>
              Future.successful(Result.from(ex))
          }

          result.flatMap {
            case Result.Success(_) =>
              TestSpec.from(property.name, tearDown)(ec)(env)
            case error =>
              silent(tearDown(env))
              Future.successful(error)
          }
        }
        catch {
          case NonFatal(ex) =>
            Future.successful(Result.from(ex))
        }
      })
  }
}

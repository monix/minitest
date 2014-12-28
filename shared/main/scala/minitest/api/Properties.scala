/*
 * Copyright (c) 2014 by Alexandru Nedelcu. Some rights reserved.
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

import scala.util.control.NonFatal
import minitest.api.Utils.silent

case class Properties[I](
  setup: () => I,
  tearDown: I => Unit,
  properties: Seq[Property[I, Unit]])
  extends Iterable[Property[Unit, Unit]] {

  def iterator: Iterator[Property[Unit, Unit]] = {
    for (property <- properties.iterator) yield
      Property[Unit, Unit](property.name, { ignore =>
        try {
          val env = setup()
          val result = try property(env) catch {
            case NonFatal(ex) =>
              Result.from(ex)
          }

          result match {
            case Result.Success(_) =>
              Property.from(property.name, tearDown)(env)
            case error =>
              silent(tearDown(env))
              error
          }
        }
        catch {
          case NonFatal(ex) =>
            Result.from(ex)
        }
      })
  }
}

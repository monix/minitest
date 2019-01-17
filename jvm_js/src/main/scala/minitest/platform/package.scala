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

import org.portablescala.reflect.Reflect

/**
 * The `platform` package provides the required Scala types for
 * source-level compatibility between JVM/JS and Native, along with
 * utilities with a platform-specific implementation.
 */
package object platform {
  /**
   * Type alias needed because Scala Native does not provide
   * the standard [[scala.concurrent.Future]] class yet.
   */
  type Future[+A] = scala.concurrent.Future[A]

  /**
   * Type alias needed because Scala Native does not provide
   * the standard [[scala.concurrent.Future]] class yet.
   */
  val Future = scala.concurrent.Future

  /**
   * Type alias needed because Scala Native does not provide
   * an implementation for [[scala.concurrent.Await]] yet.
   */
  val Await = scala.concurrent.Await

  /**
   * Type alias needed because Scala Native does not provide
   * an implementation for [[scala.concurrent.Promise]] yet.
   */
  type Promise[A] = scala.concurrent.Promise[A]

  /**
   * Type alias needed because Scala Native does not provide
   * an implementation for [[scala.concurrent.Promise]] yet.
   */
  val Promise = scala.concurrent.Promise

  /**
   * Type alias needed because Scala Native does not provide
   * an implementation for [[scala.concurrent.ExecutionContext]] yet.
   */
  type ExecutionContext = scala.concurrent.ExecutionContext

  /**
   * Type alias needed because Scala Native does not provide
   * an implementation for [[scala.concurrent.ExecutionContext]] yet.
   */
  val ExecutionContext = scala.concurrent.ExecutionContext

  type EnableReflectiveInstantiation =
    org.portablescala.reflect.annotation.EnableReflectiveInstantiation

  private[minitest] def loadModule(name: String, loader: ClassLoader): Any = {
    Reflect
      .lookupLoadableModuleClass(name + "$", loader)
      .getOrElse(throw new ClassNotFoundException(name))
      .loadModule()
  }
}

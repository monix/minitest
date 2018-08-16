package minitest

import org.scalajs.testinterface.TestUtils

trait Platform {
  type Future[+A] = scala.concurrent.Future[A]
  val Future = scala.concurrent.Future

  val Await = scala.concurrent.Await

  val Promise = scala.concurrent.Promise

  type ExecutionContext = scala.concurrent.ExecutionContext
  val ExecutionContext = scala.concurrent.ExecutionContext
}

object Platform extends Platform {
  private[minitest] def loadModule(name: String, loader: ClassLoader): AnyRef =
    TestUtils.loadModule(name, loader)
}

package minitest

import scala.scalanative.testinterface.PreloadedClassLoader

trait Platform

object Platform extends Platform {
  private[minitest] def loadModule(name: String, loader: ClassLoader): AnyRef =
    loader.asInstanceOf[PreloadedClassLoader].loadPreloaded(name)
}

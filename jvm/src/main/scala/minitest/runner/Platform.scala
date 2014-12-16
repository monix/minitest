package minitest.runner

import scala.reflect.ClassTag

object Platform {
  def loadModule[T : ClassTag](name: String, loader: ClassLoader): Option[T] = {
    val interface = implicitly[ClassTag[T]]
    val clazz = loader.loadClass(name + "$")
    val instance = clazz.getField("MODULE$").get(null)

    Option(instance).flatMap { obj =>
      if (interface.runtimeClass.isInstance(obj))
        Some(obj.asInstanceOf[T])
      else
        None
    }
  }
}

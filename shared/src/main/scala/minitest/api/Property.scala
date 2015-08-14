package minitest.api

import scala.util.control.NonFatal

case class Property[I, +O](name: String, f: I => Result[O])
  extends (I => Result[O]) {

  override def apply(v1: I): Result[O] = f(v1)
}

object Property {
  def from[Env](name: String, cb: Env => Unit): Property[Env, Unit] =
    Property(name, { env =>
      try {
        cb(env)
        Result.Success(())
      }
      catch {
        case NonFatal(ex) =>
          Result.from(ex)
      }
    })
}



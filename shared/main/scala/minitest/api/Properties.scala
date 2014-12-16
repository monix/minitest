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
          var thrownError = true

          try {
            val result = property(env)
            thrownError = false
            result
          }
          finally {
            if (thrownError)
              silent(tearDown(env))
            else
              tearDown(env)
          }
        }
        catch {
          case NonFatal(ex) =>
            Result.from(ex)
        }
      })
  }
}

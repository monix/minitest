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

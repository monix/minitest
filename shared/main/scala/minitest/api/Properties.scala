package minitest.api

import scala.util.control.NonFatal

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
          try {
            property(env)
          }
          finally {
            tearDown(env)
          }
        }
        catch {
          case NonFatal(ex) =>
            Result.Exception(ex, None)
        }
      })
  }
}

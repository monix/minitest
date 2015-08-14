package minitest

import minitest.api.{AbstractTestSuite, Asserts, Properties, Property}

trait SimpleTestSuite extends AbstractTestSuite with Asserts {
  def test(name: String)(f: => Unit): Unit =
    synchronized {
      if (isInitialized) throw new AssertionError(
        "Cannot define new tests after SimpleTestSuite was initialized")
      propertiesSeq = propertiesSeq :+ Property.from[Unit](name, _ => f)
    }

  lazy val properties: Properties[_] =
    synchronized {
      if (!isInitialized) isInitialized = true
      Properties[Unit](() => (), _ => (), propertiesSeq)
    }

  private[this] var propertiesSeq = Seq.empty[Property[Unit, Unit]]
  private[this] var isInitialized = false
}

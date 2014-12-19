package minitest

import minitest.api.{Properties, Property, TestSuite => Base}

trait SimpleTestSuite extends Base {
  def test(name: String)(f: => Unit): Unit =
    synchronized {
      assert(!isInitialized, "Cannot define new tests after TestSuite was initialized")
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

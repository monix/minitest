package minitest

import minitest.api.{Properties, Property, TestSuite => Base}

abstract class TestSuite[Env] extends Base {
  def setup(): Env
  def tearDown(env: Env): Unit

  def test(name: String)(f: Env => Unit): Unit =
    synchronized {
      assert(!isInitialized, "Cannot define new tests after TestSuite was initialized")
      propertiesSeq = propertiesSeq :+ Property.from(name, f)
    }

  lazy val properties: Properties[_] =
    synchronized {
      if (!isInitialized) isInitialized = true
      Properties(setup, tearDown, propertiesSeq)
    }

  private[this] var propertiesSeq = Seq.empty[Property[Env, Unit]]
  private[this] var isInitialized = false
}

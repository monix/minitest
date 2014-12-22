package minitest

import minitest.api.{Asserts, AbstractTestSuite, Properties, Property}

trait TestSuite[Env] extends AbstractTestSuite with Asserts {
  def setup(): Env
  def tearDown(env: Env): Unit

  def test(name: String)(f: Env => Unit): Unit =
    synchronized {
      if (isInitialized) throw new AssertionError(
        "Cannot define new tests after TestSuite was initialized")
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

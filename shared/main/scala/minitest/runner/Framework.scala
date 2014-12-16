package minitest.runner

import sbt.testing.{Framework => BaseFramework, _}

class Framework extends BaseFramework {
  def name(): String = "scala-testkit"

  private[this] object TestKitFingerprint extends SubclassFingerprint {
    val isModule = true
    def requireNoArgConstructor(): Boolean = true
    def superclassName(): String = "minitest.api.TestSuite"
  }

  def fingerprints(): Array[Fingerprint] =
    Array(TestKitFingerprint)

  def runner(args: Array[String], remoteArgs: Array[String], testClassLoader: ClassLoader): Runner =
    new Runner(args, remoteArgs, testClassLoader)

  def slaveRunner(args: Array[String], remoteArgs: Array[String], testClassLoader: ClassLoader, send: (String) => Unit): Runner =
    runner(args, remoteArgs, testClassLoader)
}

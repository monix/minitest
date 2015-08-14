package minitest.runner

import minitest.runner.Framework.ModuleFingerprint
import sbt.testing.{Framework => BaseFramework, _}

class Framework extends BaseFramework {
  def name(): String = "scala-testkit"

  def fingerprints(): Array[Fingerprint] =
    Array(ModuleFingerprint)

  def runner(args: Array[String], remoteArgs: Array[String], testClassLoader: ClassLoader): Runner =
    new Runner(args, remoteArgs, testClassLoader)

  def slaveRunner(args: Array[String], remoteArgs: Array[String], testClassLoader: ClassLoader, send: (String) => Unit): Runner =
    runner(args, remoteArgs, testClassLoader)
}

object Framework {
  /**
   * A fingerprint that searches only for singleton objects
   * of type [[minitest.api.AbstractTestSuite]].
   */
  object ModuleFingerprint extends SubclassFingerprint {
    val isModule = true
    def requireNoArgConstructor(): Boolean = true
    def superclassName(): String = "minitest.api.AbstractTestSuite"
  }
}
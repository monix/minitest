# MiniTest

A mini testing framework cross-compiled for 
Scala 2.10, 2.11 and [Scala.js 0.6.x](http://www.scala-js.org/).

## Usage in SBT

For the JVM, in the main `build.sbt`:

```scala
libraryDependencies += "io.monix" %% "minitest" % "0.14" % "test"

testFrameworks += new TestFramework("minitest.runner.Framework")
```

For Scala.js, in the main `build.sbt`:

```scala
libraryDependencies += "io.monix" %%% "minitest" % "0.14" % "test"

testFrameworks += new TestFramework("minitest.runner.Framework")
```

## Tutorial

Test suites MUST BE objects, not classes. To create a simple test suite, it could
inherit from [SimpleTestSuite](shared/main/scala/minitest/SimpleTestSuite.scala):

Here's a simple test:

```scala
import minitest._

object MySimpleSuite extends SimpleTestSuite {
  test("should be") {
    assertEquals(2, 1 + 1)
  }

  test("should not be") {
    assert(1 + 1 != 3)
  }

  test("should throw") {
    class DummyException extends RuntimeException("DUMMY")
    def test(): String = throw new DummyException

    intercept[DummyException] {
      test()
    }
  }

  test("test result of") {
    assertResult("hello world") {
      "hello" + " " + "world"
    }
  }
}
```

In case you want to setup an environment for each test and need `setup` and
`tearDown` semantics, you could inherit from
[TestSuite](shared/main/scala/minitest/TestSuite.scala). Then on each `test` definition,
you'll receive a fresh value:

```scala
import minitest.TestSuite

object MyTestSuite extends TestSuite[Int] {
  def setup(): Int = {
    Random.nextInt(100) + 1
  }

  def tearDown(env: Int): Unit = {
    assert(env > 0, "should still be positive")
  }

  test("should be positive") { env =>
    assert(env > 0, "positive test")
  }

  test("should be lower or equal to 100") { env =>
    assert(env <= 100, s"$env > 100")
  }
}
```

That's all you need to know.

## License

All code in this repository is licensed under the Apache License, Version 2.0.
See [LICENCE](./LICENSE).

Copyright &copy; 2014 Alexandru Nedelcu

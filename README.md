# MiniTest

A mini testing framework for Scala, cross-compiled for
[Scala.js](http://www.scala-js.org/) (currently `0.6.0-M2`).

## Usage in SBT

For the JVM, in the main `build.sbt`:

```scala
libraryDependencies += "org.monifu" %% "minitest" % "0.3" % "test"

testFrameworks += new TestFramework("minitest.runner.Framework")
```

For Scala.js, in the main `build.sbt`:

```scala
libraryDependencies += "org.monifu" %%% "minitest" % "0.3" % "test"

testFrameworks += new TestFramework("minitest.runner.Framework")
```

NOTE: At this point it is compiled for Scala.js
[milestone 0.6.0-M2](http://www.scala-js.org/news/2014/12/05/announcing-scalajs-0.6.0-M2/),
and won't work with the stable `0.5.x`.

## Tutorial

Test suites MUST BE objects, not classes. To create a simple test suite, it could
inherit from [SimpleTestSuite](shared/main/scala/minitest/SimpleTestSuite.scala):

Here's a simple test:

```scala
import minitest.SimpleTestSuite

object MySimpleSuite extends SimpleTestSuite {
  test("should be") {
    expect(1 + 1).toBe(2)
  }

  test("should not be") {
    expect(1 + 1).toNotBe(3)
  }

  test("should be true") {
    expect(1 + 1 == 2).toBeTrue
  }

  test("should be false") {
    expect(1 + 1 == 3).toBeFalse
  }

  test("should throw") {
    class DummyException extends RuntimeException("DUMMY")
    def test(): String = throw new DummyException

    expect(test()).toThrow[DummyException]
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
    expect(env > 0).toBe(true)
  }

  test("should be positive") { env =>
    expect(env > 0).toBeTrue
  }

  test("should be lower or equal to 100") { env =>
    expect(env <= 100).toBeTrue
  }

  test("should do addition") { env =>
    expect(env + 1).toNotBe(env)
  }
}
```

That's all you need to know.

## License

All code in this repository is licensed under the Apache License, Version 2.0.
See [LICENCE](./LICENSE).

Copyright &copy; 2014 Alexandru Nedelcu



# Minitest

A mini testing framework cross-compiled for Scala 2.10, 2.11, 2.12 and
[Scala.js 0.6.x](http://www.scala-js.org/).

## Usage in SBT

For `build.sbt` (use the `%%%` operator for Scala.js):

```scala
// use the %%% operator for Scala.js
libraryDependencies += "io.monix" %% "minitest" % "0.27" % "test"

testFrameworks += new TestFramework("minitest.runner.Framework")
```

In case you want the optional package for [ScalaCheck](https://www.scalacheck.org/)
integration:

```scala
// use the %%% operator for Scala.js
libraryDependencies += "io.monix" %% "minitest-laws" % "0.27" % "test"
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

Minitest supports asynchronous results in tests, just return a `Future[Unit]`:

```scala
import scala.concurrent.ExecutionContext.Implicits.global

object MySimpleSuite extends SimpleTestSuite {
  test("asynchronous execution") {
    val future = Future(100).map(_+1)
    
    for (result <- future) yield {
      assertEquals(result, 101)
    }
  }
}
```

Minitest has integration with [ScalaCheck](https://www.scalacheck.org/). 
So for property-based testing:

```scala
import minitest.laws.Checkers

object MyLawsTest extends SimpleTestSuite with Checkers {
  test("addition of integers is commutative") {
    check2((x: Int, y: Int) => x + y == y + x)
  }
  
  test("addition of integers is transitive") {
    check3((x: Int, y: Int, z: Int) => (x + y) + z == x + (y + z))
  }
}
```

That's all you need to know.

## License

All code in this repository is licensed under the Apache License, Version 2.0.
See [LICENCE](./LICENSE).

Copyright &copy; 2014-2016 Alexandru Nedelcu

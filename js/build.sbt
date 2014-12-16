name := "minitest"

libraryDependencies += "org.scala-js" %% "scalajs-test-interface" % scalaJSVersion

testFrameworks := Seq(new TestFramework("minitest.runner.Framework"))

scalaJSStage in Test := FastOptStage
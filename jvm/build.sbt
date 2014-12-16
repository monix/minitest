name := "minitest"

libraryDependencies ++= Seq(
  "org.scala-sbt" % "test-interface" % "1.0",
  "org.scala-js" %% "scalajs-stubs" % scalaJSVersion % "provided"
)

testFrameworks := Seq(new TestFramework("minitest.runner.Framework"))
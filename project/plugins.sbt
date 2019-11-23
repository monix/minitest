val crossProjVersion = "0.6.1"
val scalaJSVersion =
  Option(System.getenv("SCALAJS_VERSION")).getOrElse("0.6.31")
val scalaNativeVersion =
  Option(System.getenv("SCALANATIVE_VERSION")).getOrElse("0.3.9")

addSbtPlugin("org.portable-scala" % "sbt-scalajs-crossproject"      % crossProjVersion)
addSbtPlugin("org.portable-scala" % "sbt-scala-native-crossproject" % crossProjVersion)
addSbtPlugin("org.scala-js"       % "sbt-scalajs"                   % scalaJSVersion)
addSbtPlugin("org.scala-native"   % "sbt-scala-native"              % scalaNativeVersion)
addSbtPlugin("com.jsuereth"       % "sbt-pgp"                       % "1.1.2")
addSbtPlugin("com.typesafe.sbt"   % "sbt-git"                       % "1.0.0")
addSbtPlugin("org.xerial.sbt"     % "sbt-sonatype"                  % "2.3")

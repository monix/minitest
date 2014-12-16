import com.typesafe.sbt.pgp.PgpKeys
import sbt.Keys._
import sbt.{Build => SbtBuild, _}
import org.scalajs.sbtplugin.ScalaJSPlugin
import sbtrelease.ReleasePlugin._
import sbtrelease.ReleasePlugin.ReleaseKeys._

object Build extends SbtBuild {
  val baseSettings = releaseSettings ++ Seq(
    organization := "org.monifu",

    scalaVersion := "2.11.4",
    crossScalaVersions := Seq("2.11.4", "2.10.4"),
    publishArtifactsAction := PgpKeys.publishSigned.value,
    crossBuild := true,

    // -- Settings meant for deployment on oss.sonatype.org

    publishMavenStyle := true,

    publishTo := {
      val nexus = "https://oss.sonatype.org/"
      if (isSnapshot.value)
        Some("snapshots" at nexus + "content/repositories/snapshots")
      else
        Some("releases"  at nexus + "service/local/staging/deploy/maven2")
    },

    publishArtifact in Test := false,
    pomIncludeRepository := { _ => false },

    pomExtra :=
      <url>https://github.com/monifu/minitest/</url>
        <licenses>
          <license>
            <name>Apache License, Version 2.0</name>
            <url>https://www.apache.org/licenses/LICENSE-2.0</url>
            <distribution>repo</distribution>
          </license>
        </licenses>
        <scm>
          <url>git@github.com:monifu/minitest.git</url>
          <connection>scm:git:git@github.com:monifu/minitest.git</connection>
        </scm>
        <developers>
          <developer>
            <id>alex_ndc</id>
            <name>Alexandru Nedelcu</name>
            <url>https://www.bionicspirit.com/</url>
          </developer>
        </developers>
  )

  val sharedSettings = baseSettings ++ Seq(
    unmanagedSourceDirectories in Compile <+= baseDirectory(_ /  "shared" / "main" / "scala"),
    unmanagedSourceDirectories in Test <+= baseDirectory(_ / "shared" / "test" / "scala"),

    scalacOptions <<= baseDirectory.map { bd => Seq("-sourcepath", bd.getAbsolutePath) },

    scalacOptions ++= Seq(
      "-unchecked", "-deprecation", "-feature", "-Xlint", "-target:jvm-1.6",
      "-Yinline-warnings", "-optimise", "-Ywarn-adapted-args",
      "-Ywarn-dead-code", "-Ywarn-inaccessible", "-Ywarn-nullary-override",
      "-Ywarn-nullary-unit", "-Xlog-free-terms"
    ),

    resolvers ++= Seq(
      "Typesafe Releases" at "http://repo.typesafe.com/typesafe/releases",
      Resolver.sonatypeRepo("releases")
    ),

    libraryDependencies <+= scalaVersion("org.scala-lang" % "scala-reflect" % _ % "compile")
  )

  // -- Root aggregating everything
  lazy val root = project.in(file("."))
    .aggregate(jvm, js)
    .settings(baseSettings : _*)
    .settings(
      name := "root",
      publishArtifact := false,
      publishArtifact in (Compile, packageDoc) := false,
      publishArtifact in (Compile, packageSrc) := false,
      publishArtifact in (Compile, packageBin) := false
    )

  lazy val jvm = project.in(file("jvm"))
    .settings(sharedSettings : _*)

  lazy val js = project.in(file("js"))
    .settings(sharedSettings : _*)
    .enablePlugins(ScalaJSPlugin)
}

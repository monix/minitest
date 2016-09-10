/*
 * Copyright (c) 2014-2016 by Alexandru Nedelcu.
 * Some rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import sbt._
import com.typesafe.sbt.pgp.PgpKeys
import org.scalajs.sbtplugin.ScalaJSPlugin
import org.scalajs.sbtplugin.ScalaJSPlugin.autoImport._
import sbt.Keys._
import sbtrelease.ReleasePlugin.autoImport._

lazy val baseSettings = Seq(
  organization := "io.monix",
  scalaVersion := "2.11.8",
  crossScalaVersions := Seq("2.11.8", "2.10.6", "2.12.0-RC1"),
  releasePublishArtifactsAction := PgpKeys.publishSigned.value,
  releaseCrossBuild := true,

  // -- Settings meant for deployment on oss.sonatype.org

  useGpg := true,
  useGpgAgent := true,
  usePgpKeyHex("2673B174C4071B0E"),
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
    <url>https://github.com/monixio/minitest/</url>
      <licenses>
        <license>
          <name>Apache License, Version 2.0</name>
          <url>https://www.apache.orsg/licenses/LICENSE-2.0</url>
          <distribution>repo</distribution>
        </license>
      </licenses>
      <scm>
        <url>git@github.com:monix/minitest.git</url>
        <connection>scm:git:git@github.com:monixio/minitest.git</connection>
      </scm>
      <developers>
        <developer>
          <id>alexelcu</id>
          <name>Alexandru Nedelcu</name>
          <url>https://alexn.org/</url>
        </developer>
      </developers>
)

lazy val crossVersionSharedSources =
  Seq(Compile, Test).map { sc =>
    (unmanagedSourceDirectories in sc) ++= {
      (unmanagedSourceDirectories in sc ).value.map {
        dir:File => new File(dir.getPath + "_" + scalaBinaryVersion.value)
      }
    }}

lazy val scalaLinterOptions =
  Seq(
    // Enables linter options
    "-Xlint:adapted-args", // warn if an argument list is modified to match the receiver
    "-Xlint:nullary-unit", // warn when nullary methods return Unit
    "-Xlint:inaccessible", // warn about inaccessible types in method signatures
    "-Xlint:nullary-override", // warn when non-nullary `def f()' overrides nullary `def f'
    "-Xlint:infer-any", // warn when a type argument is inferred to be `Any`
    "-Xlint:missing-interpolator", // a string literal appears to be missing an interpolator id
    "-Xlint:doc-detached", // a ScalaDoc comment appears to be detached from its element
    "-Xlint:private-shadow", // a private field (or class parameter) shadows a superclass field
    "-Xlint:type-parameter-shadow", // a local type parameter shadows a type already in scope
    "-Xlint:poly-implicit-overload", // parameterized overloaded implicit methods are not visible as view bounds
    "-Xlint:option-implicit", // Option.apply used implicit view
    "-Xlint:delayedinit-select", // Selecting member of DelayedInit
    "-Xlint:by-name-right-associative", // By-name parameter of right associative operator
    "-Xlint:package-object-classes", // Class or object defined in package object
    "-Xlint:unsound-match" // Pattern match may not be typesafe
  )

lazy val sharedSettings = baseSettings ++ Seq(
  unmanagedSourceDirectories in Compile <+= baseDirectory(_.getParentFile / "shared" / "src" / "main" / "scala"),
  unmanagedSourceDirectories in Compile <+= baseDirectory(_.getParentFile / "shared" / "src" / "main" / "scala"),
  unmanagedSourceDirectories in Test <+= baseDirectory(_.getParentFile / "shared" / "src" / "test" / "scala"),

  scalacOptions <<= baseDirectory.map { bd => Seq("-sourcepath", bd.getAbsolutePath) },

  scalacOptions ++= Seq(
    "-unchecked", "-deprecation", "-feature", "-Xlint",
    "-Ywarn-adapted-args", "-Ywarn-dead-code", "-Ywarn-inaccessible",
    "-Ywarn-nullary-override", "-Ywarn-nullary-unit",
    "-Xlog-free-terms"
  ),

  // Version specific options
  scalacOptions ++= (CrossVersion.partialVersion(scalaVersion.value) match {
    case Some((2, 12)) =>
      scalaLinterOptions
    case Some((2, 11)) =>
      scalaLinterOptions ++ Seq("-target:jvm-1.6")
    case _ =>
      Seq("-target:jvm-1.6")
  }),

  resolvers ++= Seq(
    "Typesafe Releases" at "http://repo.typesafe.com/typesafe/releases",
    Resolver.sonatypeRepo("releases")
  ),

  libraryDependencies <+= scalaVersion("org.scala-lang" % "scala-reflect" % _ % "compile"),
  testFrameworks := Seq(new TestFramework("minitest.runner.Framework"))
)

lazy val scalaJSSettings = Seq(
  scalaJSStage in Test := FastOptStage,
  scalaJSUseRhino in Global := false
)

lazy val requiredMacroCompatDeps = Seq(
  libraryDependencies ++= (CrossVersion.partialVersion(scalaVersion.value) match {
    case Some((2, majorVersion)) if majorVersion >= 11 =>
      Seq(
        "org.scala-lang" % "scala-reflect" % scalaVersion.value % "provided",
        "org.scala-lang" % "scala-compiler" % scalaVersion.value % "provided",
        "org.typelevel" %%% "macro-compat" % "1.1.1" % "provided",
        compilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full)
      )
    case _ =>
      Seq(
        "org.scala-lang" % "scala-reflect" % scalaVersion.value % "provided",
        "org.scala-lang" % "scala-compiler" % scalaVersion.value % "provided",
        "org.typelevel" %%% "macro-compat" % "1.1.1",
        compilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full)
      )
  }))

lazy val minitest = project.in(file("."))
  .aggregate(minitestJVM, minitestJS, lawsJVM, lawsJS)
  .settings(baseSettings)
  .settings(
    publishArtifact := false,
    publishArtifact in (Compile, packageDoc) := false,
    publishArtifact in (Compile, packageSrc) := false,
    publishArtifact in (Compile, packageBin) := false
  )

lazy val minitestJVM = project.in(file("jvm"))
  .settings(sharedSettings)
  .settings(crossVersionSharedSources)
  .settings(requiredMacroCompatDeps)
  .settings(
    name := "minitest",
    libraryDependencies ++= Seq(
      "org.scala-sbt" % "test-interface" % "1.0",
      "org.scala-js" %% "scalajs-stubs" % scalaJSVersion % "provided"
    ))

lazy val minitestJS = project.in(file("js"))
  .enablePlugins(ScalaJSPlugin)
  .settings(sharedSettings)
  .settings(crossVersionSharedSources)
  .settings(scalaJSSettings)
  .settings(requiredMacroCompatDeps)
  .settings(
    name := "minitest",
    libraryDependencies += "org.scala-js" %% "scalajs-test-interface" % scalaJSVersion
  )

lazy val lawsSettings = Seq(
  name := "minitest-laws",
  libraryDependencies ++= Seq(
    "org.scalacheck" %%% "scalacheck" % "1.13.2"
  ))

lazy val lawsJVM = project.in(file("laws/jvm"))
  .dependsOn(minitestJVM)
  .settings(sharedSettings)
  .settings(lawsSettings)

lazy val lawsJS = project.in(file("laws/js"))
  .enablePlugins(ScalaJSPlugin)
  .dependsOn(minitestJS)
  .settings(sharedSettings)
  .settings(scalaJSSettings)
  .settings(lawsSettings)

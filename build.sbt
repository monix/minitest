/*
 * Copyright (c) 2014-2019 by The Minitest Project Developers.
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

import sbt.{url, _}
import org.scalajs.sbtplugin.ScalaJSPlugin
import org.scalajs.sbtplugin.ScalaJSPlugin.autoImport._
import sbt.Keys._
import com.typesafe.sbt.GitVersioning

addCommandAlias("ci-all",  ";+clean ;+test:compile ;+test ; +package")
addCommandAlias("release", ";+clean ;+package ;+publishSigned")

val Scala212 = "2.12.14"

ThisBuild / scalaVersion := Scala212
ThisBuild / crossScalaVersions := Seq(Scala212, "2.13.5", "3.0.1")

ThisBuild / scalacOptions ++= Seq(
  // Note, this is used by the doc-source-url feature to determine the
  // relative path of a given source file. If it's not a prefix of a the
  // absolute path of the source file, the absolute path of that file
  // will be put into the FILE_SOURCE variable, which is
  // definitely not what we want.
  "-sourcepath", file(".").getAbsolutePath.replaceAll("[.]$", "")
)

inThisBuild(List(
  organization := "io.monix",
  homepage := Some(url("https://monix.io")),
  licenses := Seq("APL2" -> url("http://www.apache.org/licenses/LICENSE-2.0.txt")),
  developers := List(
    Developer(
      id="Avasil",
      name="Piotr Gawrys",
      email="pgawrys2@gmail.com",
      url=url("https://github.com/Avasil")
    ))
))

lazy val sharedSettings = Seq(
  // Version specific options
  scalacOptions ++= (
    if (isDotty.value)
      Seq()
    else
      Seq(
        "-unchecked", "-deprecation", "-feature", "-Xlint",
        "-Ywarn-dead-code",
        "-Xlog-free-terms",
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
        "-Xlint:package-object-classes" // Class or object defined in package object
      )

  ),
  Compile / doc / sources := {
    val old = (Compile / doc / sources).value
    if (isDotty.value)
      Seq()
    else
      old
  },
  testFrameworks := Seq(new TestFramework("minitest.runner.Framework"))
)

lazy val scalaJSSettings = Seq(
  scalaJSStage in Test := FastOptStage
)

lazy val minitestRoot = project.in(file("."))
  .aggregate(minitestJVM, minitestJS, lawsJVM, lawsJS)
  .settings(
    name := "minitest root",
    Compile / sources := Nil,
    skip in publish := true,
  )

lazy val minitest = crossProject(JVMPlatform, JSPlatform).in(file("."))
  .settings(
    name := "minitest",
    sharedSettings,
    Compile / unmanagedSourceDirectories += (
      if (isDotty.value)
        (ThisBuild / baseDirectory).value / "shared/src/main/scala-3"
      else
        (ThisBuild / baseDirectory).value / "shared/src/main/scala-2"
    ),
    libraryDependencies ++= (
      if (isDotty.value)
        Seq()
      else
        Seq(
          "org.scala-lang" % "scala-reflect" % scalaVersion.value % Compile
        )
    ),
    libraryDependencies +=
      ("org.portable-scala" %%% "portable-scala-reflect" % "1.1.1").withDottyCompat(scalaVersion.value)
  )
  .jvmSettings(
    libraryDependencies ++= Seq(
      ("org.scala-sbt" % "test-interface" % "1.0").withDottyCompat(scalaVersion.value)
    ),
  )
  .jsSettings(
    scalaJSSettings,
    libraryDependencies += ("org.scala-js" %% "scalajs-test-interface" % scalaJSVersion).withDottyCompat(scalaVersion.value)
  )

lazy val minitestJVM    = minitest.jvm
lazy val minitestJS     = minitest.js

lazy val laws = crossProject(JVMPlatform, JSPlatform)
  .crossType(CrossType.Pure)
  .in(file("laws"))
  .dependsOn(minitest)
  .settings(
    name := "minitest-laws",
    sharedSettings,
    libraryDependencies ++= Seq(
      "org.scalacheck" %%% "scalacheck" % "1.15.4"
    )
  )
  .jsSettings(
    scalaJSSettings
  )

lazy val lawsJVM    = laws.jvm
lazy val lawsJS     = laws.js

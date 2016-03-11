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

import com.typesafe.sbt.pgp.PgpKeys
import org.scalajs.sbtplugin.ScalaJSPlugin
import org.scalajs.sbtplugin.ScalaJSPlugin.autoImport._
import sbt.Keys._
import sbt.{Build => SbtBuild, _}
import sbtrelease.ReleasePlugin.autoImport._

object Build extends SbtBuild {
  val baseSettings = Seq(
    organization := "io.monix",
    scalaVersion := "2.11.7",
    crossScalaVersions := Seq("2.11.7", "2.10.6"),
    releasePublishArtifactsAction := PgpKeys.publishSigned.value,
    releaseCrossBuild := true,

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
          <connection>scm:git:git@github.com:monix/minitest.git</connection>
        </scm>
        <developers>
          <developer>
            <id>alexelcu</id>
            <name>Alexandru Nedelcu</name>
            <url>https://bionicspirit.com/</url>
          </developer>
        </developers>
  )

  val sharedSettings = baseSettings ++ Seq(
    unmanagedSourceDirectories in Compile <+= baseDirectory(_.getParentFile / "shared" / "src" / "main" / "scala"),
    unmanagedSourceDirectories in Test <+= baseDirectory(_.getParentFile / "shared" / "src" / "test" / "scala"),

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

    libraryDependencies <+= scalaVersion("org.scala-lang" % "scala-reflect" % _ % "compile"),
    libraryDependencies ++= Seq(
      "org.typelevel" %% "macro-compat" % "1.1.0",
      compilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full)
    ),

    testFrameworks := Seq(new TestFramework("minitest.runner.Framework"))
  )

  lazy val scalaJSSettings = Seq(
    scalaJSStage in Test := FastOptStage,
    scalaJSUseRhino in Global := false
  )

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
    .settings(
      name := "minitest",
      libraryDependencies ++= Seq(
        "org.scala-sbt" % "test-interface" % "1.0",
        "org.scala-js" %% "scalajs-stubs" % scalaJSVersion % "provided"
      ))

  lazy val minitestJS = project.in(file("js"))
    .enablePlugins(ScalaJSPlugin)
    .settings(sharedSettings)
    .settings(scalaJSSettings)
    .settings(
      name := "minitest",
      libraryDependencies += "org.scala-js" %% "scalajs-test-interface" % scalaJSVersion
    )

  lazy val lawsSettings = Seq(
    name := "minitest-laws",
    libraryDependencies ++= Seq(
      "org.typelevel" %%% "discipline" % "0.4",
      "org.scalacheck" %%% "scalacheck" % "1.12.5"
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
}

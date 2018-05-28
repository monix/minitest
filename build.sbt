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
import org.scalajs.sbtplugin.ScalaJSPlugin
import org.scalajs.sbtplugin.ScalaJSPlugin.autoImport._
import sbt.Keys._
import com.typesafe.sbt.GitVersioning
import scala.xml.Elem
import scala.xml.transform.{RewriteRule, RuleTransformer}

addCommandAlias("ci-all",  ";+clean ;+compile ;+test ;+package")
addCommandAlias("release", ";+publishSigned ;sonatypeReleaseAll")

lazy val baseSettings = Seq(
  organization := "io.monix",
  scalaVersion := "2.12.4",
  crossScalaVersions := Seq("2.10.7", "2.11.12", "2.12.4", "2.13.0-M3"),

  // -- Settings meant for deployment on oss.sonatype.org
  sonatypeProfileName := organization.value,

  publishMavenStyle := true,
  publishTo := {
    val nexus = "https://oss.sonatype.org/"
    if (isSnapshot.value)
      Some("snapshots" at nexus + "content/repositories/snapshots")
    else
      Some("releases"  at nexus + "service/local/staging/deploy/maven2")
  },

  isSnapshot := version.value endsWith "SNAPSHOT",
  publishArtifact in Test := false,
  pomIncludeRepository := { _ => false }, // removes optional dependencies

  // For evicting Scoverage out of the generated POM
  // See: https://github.com/scoverage/sbt-scoverage/issues/153
  pomPostProcess := { (node: xml.Node) =>
    new RuleTransformer(new RewriteRule {
      override def transform(node: xml.Node): Seq[xml.Node] = node match {
        case e: Elem
          if e.label == "dependency" && e.child.exists(child => child.label == "groupId" && child.text == "org.scoverage") => Nil
        case _ => Seq(node)
      }
    }).transform(node).head
  },

  licenses := Seq("APL2" -> url("http://www.apache.org/licenses/LICENSE-2.0.txt")),
  homepage := Some(url("https://github.com/monix/minitest")),

  scmInfo := Some(
    ScmInfo(
      url("https://github.com/monix/minitest"),
      "scm:git@github.com:monix/minitest.git"
    )),

  developers := List(
    Developer(
      id="alexelcu",
      name="Alexandru Nedelcu",
      email="noreply@alexn.org",
      url=url("https://alexn.org")
    ))
)

def scalaPartV = Def setting (CrossVersion partialVersion scalaVersion.value)
lazy val crossVersionSharedSources: Seq[Setting[_]] =
  Seq(Compile, Test).map { sc =>
    (unmanagedSourceDirectories in sc) ++= {
      (unmanagedSourceDirectories in sc).value.map { dir =>
        scalaPartV.value match {
          case Some((major, minor)) =>
            new File(dir.getPath + s"_$major.$minor")
          case None =>
            throw new NoSuchElementException("Scala version")
        }
      }
    }
  }

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
  unmanagedSourceDirectories in Compile += {
    baseDirectory.value.getParentFile / "shared" / "src" / "main" / "scala"
  },
  unmanagedSourceDirectories in Test += {
    baseDirectory.value.getParentFile / "shared" / "src" / "test" / "scala"
  },

  scalacOptions in ThisBuild ++= Seq(
    // Note, this is used by the doc-source-url feature to determine the
    // relative path of a given source file. If it's not a prefix of a the
    // absolute path of the source file, the absolute path of that file
    // will be put into the FILE_SOURCE variable, which is
    // definitely not what we want.
    "-sourcepath", file(".").getAbsolutePath.replaceAll("[.]$", "")
  ),

  scalacOptions ++= Seq(
    "-unchecked", "-deprecation", "-feature", "-Xlint",
    "-Ywarn-adapted-args", "-Ywarn-dead-code", "-Ywarn-inaccessible",
    "-Ywarn-nullary-override", "-Ywarn-nullary-unit",
    "-Xlog-free-terms"
  ),

  // Version specific options
  scalacOptions ++= (CrossVersion.partialVersion(scalaVersion.value) match {
    case Some((2, v)) if v >= 12 =>
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

  testFrameworks := Seq(new TestFramework("minitest.runner.Framework"))
)

lazy val scalaJSSettings = Seq(
  scalaJSStage in Test := FastOptStage
)

lazy val needsScalaParadise = settingKey[Boolean]("Needs Scala Paradise")

lazy val requiredMacroCompatDeps = Seq(
  needsScalaParadise := {
    val sv = scalaVersion.value
    (sv startsWith "2.10.") || (sv startsWith "2.11.") || (sv startsWith "2.12.") || (sv == "2.13.0-M3")
  },
  libraryDependencies ++= Seq(
    "org.scala-lang" % "scala-reflect" % scalaVersion.value % Compile,
    "org.scala-lang" % "scala-compiler" % scalaVersion.value % Provided,
    "org.typelevel" %%% "macro-compat" % "1.1.1",
  ),
  libraryDependencies ++= {
    if (needsScalaParadise.value) Seq(compilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.patch))
    else Nil
  },
  scalacOptions ++= {
    if (needsScalaParadise.value) Nil
    else Seq("-Ymacro-annotations")
  }
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
    "org.scalacheck" %%% "scalacheck" % "1.14.0"
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

//------------- For Release

enablePlugins(GitVersioning)

/* The BaseVersion setting represents the in-development (upcoming) version,
 * as an alternative to SNAPSHOTS.
 */
git.baseVersion := "2.0.0"

val ReleaseTag = """^v(\d+\.\d+\.\d+(?:[-.]\w+)?)$""".r
git.gitTagToVersionNumber := {
  case ReleaseTag(v) => Some(v)
  case _ => None
}

git.formattedShaVersion := {
  val suffix = git.makeUncommittedSignifierSuffix(git.gitUncommittedChanges.value, git.uncommittedSignifier.value)

  git.gitHeadCommit.value map { _.substring(0, 7) } map { sha =>
    git.baseVersion.value + "-" + sha + suffix
  }
}

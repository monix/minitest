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

import scala.xml.Elem
import scala.xml.transform.{RewriteRule, RuleTransformer}

ThisBuild / organization := "io.monix"
ThisBuild / licenses := Seq("APL2" -> url("http://www.apache.org/licenses/LICENSE-2.0.txt"))
ThisBuild / homepage := Some(url("https://github.com/monix/minitest"))

ThisBuild / scmInfo := Some(
  ScmInfo(
    url("https://github.com/monix/minitest"),
    "scm:git@github.com:monix/minitest.git"
  ))

ThisBuild / developers := List(
  Developer(
    id="alexelcu",
    name="Alexandru Nedelcu",
    email="noreply@alexn.org",
    url=url("https://alexn.org")
  ))

// -- Settings meant for deployment on oss.sonatype.org
ThisBuild / sonatypeProfileName := (ThisBuild / organization).value
ThisBuild / publishMavenStyle := true
ThisBuild / publishTo := {
  val nexus = "https://oss.sonatype.org/"
  if (isSnapshot.value)
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("releases"  at nexus + "service/local/staging/deploy/maven2")
}
ThisBuild / isSnapshot := {
  (ThisBuild / version).value endsWith "SNAPSHOT"
}
ThisBuild / Test / publishArtifact := false
ThisBuild / pomIncludeRepository := { _ => false } // removes optional dependencies

// For evicting Scoverage out of the generated POM
// See: https://github.com/scoverage/sbt-scoverage/issues/153
ThisBuild / pomPostProcess := { (node: xml.Node) =>
  new RuleTransformer(new RewriteRule {
    override def transform(node: xml.Node): Seq[xml.Node] = node match {
      case e: Elem
        if e.label == "dependency" && e.child.exists(child => child.label == "groupId" && child.text == "org.scoverage") => Nil
      case _ => Seq(node)
    }
  }).transform(node).head
}

enablePlugins(GitVersioning)

/* The BaseVersion setting represents the in-development (upcoming) version,
 * as an alternative to SNAPSHOTS.
 */
git.baseVersion := "2.3.2"

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

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

package minitest.laws

import org.scalacheck.Prop
import org.typelevel.discipline._

object Dummy {
  def prop = Prop(_ => Prop.Result(status = Prop.True))
}

trait GroupLaws extends Laws {
  def semigroup = new GroupProperties(
    name = "semigroup",
    parent = None,
    "associative" → Dummy.prop
  )

  def monoid = new GroupProperties(
    name = "monoid",
    parent = Some(semigroup),
    "identity" → Dummy.prop
  )

  def group = new GroupProperties(
    name = "group",
    parent = Some(monoid),
    "inverse" → Dummy.prop
  )

  def additiveSemigroup = new AdditiveProperties(
    base = semigroup,
    parent = None
  )

  def additiveMonoid = new AdditiveProperties(
    base = monoid,
    parent = Some(additiveSemigroup)
  )

  def additiveGroup = new AdditiveProperties(
    base = group,
    parent = Some(additiveMonoid)
  )

  class GroupProperties(
    name: String,
    parent: Option[GroupProperties],
    props: (String, Prop)*
  ) extends DefaultRuleSet(name, parent, props: _*)

  class AdditiveProperties(
    val base: GroupProperties,
    val parent: Option[AdditiveProperties],
    val props: (String, Prop)*
  ) extends RuleSet with HasOneParent {
    val name = base.name
    val bases = Seq("base" → base)
  }
}

object RingLaws extends GroupLaws { self =>
  def multiplicativeSemigroup = new MultiplicativeProperties(
    base = _.semigroup,
    parent = None
  )

  def multiplicativeMonoid = new MultiplicativeProperties(
    base = _.monoid,
    parent = Some(multiplicativeSemigroup)
  )

  def multiplicativeGroup = new MultiplicativeProperties(
    base = _.group,
    parent = Some(multiplicativeMonoid),
    "reciprocal consistent" → Dummy.prop
  )

  def semiring = new RingProperties(
    name = "semiring",
    al = additiveSemigroup,
    ml = multiplicativeSemigroup,
    parents = Seq.empty,
    "distributive" → Dummy.prop
  )

  def rng = new RingProperties(
    name = "rng",
    al = additiveGroup, // not exactly, but hey
    ml = multiplicativeSemigroup,
    parents = Seq(semiring)
  )

  def rig = new RingProperties(
    name = "rig",
    al = additiveMonoid,
    ml = multiplicativeMonoid,
    parents = Seq(semiring)
  )

  def ring = new RingProperties(
    name = "ring",
    al = additiveGroup,
    ml = multiplicativeMonoid,
    parents = Seq(rig, rng)
  )

  class MultiplicativeProperties(
    val base: GroupLaws => GroupLaws#GroupProperties,
    val parent: Option[MultiplicativeProperties],
    val props: (String, Prop)*
  ) extends RuleSet with HasOneParent {
    private val _base = base(RingLaws.this)

    val name = _base.name
    val bases = Seq("base" → _base)
  }

  class RingProperties(
    val name: String,
    val al: AdditiveProperties,
    val ml: MultiplicativeProperties,
    val parents: Seq[RingProperties],
    val props: (String, Prop)*
  ) extends RuleSet {
    def bases = Seq("additive" → al, "multiplicative" → ml)
  }
}
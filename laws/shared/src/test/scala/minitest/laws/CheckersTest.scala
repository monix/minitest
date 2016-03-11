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

import minitest.SimpleTestSuite
import minitest.api.AssertionException

object CheckersTest extends SimpleTestSuite with Checkers {
  test("check1 property should succeed") {
    check1((x: Int) => x == x)
  }

  test("check1 property should fail") {
    intercept[AssertionException] {
      check1((x: Int) => x != x)
    }
  }

  test("check2 property should succeed") {
    check2((x: Int, y: Int) => x + y == x + y)
  }

  test("check2 property should fail") {
    intercept[AssertionException] {
      check2((x: Int, y: Int) => x + y != x + y)
    }
  }

  test("check3 property should succeed") {
    check3((x: Int, y: Int, z: Int) => x + y + z == x + y + z)
  }

  test("check3 property should fail") {
    intercept[AssertionException] {
      check3((x: Int, y: Int, z: Int) => x + y + z != x + y + z)
    }
  }

  test("check4 property should succeed") {
    check4((x1:Int,x2:Int,x3:Int,x4:Int) => x1+x2+x3+x4==x1+x2+x3+x4)
  }

  test("check4 property should fail") {
    intercept[AssertionException] {
      check4((x1:Int,x2:Int,x3:Int,x4:Int) => x1+x2+x3+x4!=x1+x2+x3+x4)
    }
  }

  test("check5 property should succeed") {
    check5((x1:Int,x2:Int,x3:Int,x4:Int,x5:Int) => x1+x2+x3+x4+x5==x1+x2+x3+x4+x5)
  }

  test("check5 property should fail") {
    intercept[AssertionException] {
      check5((x1:Int,x2:Int,x3:Int,x4:Int,x5:Int) => x1+x2+x3+x4+x5!=x1+x2+x3+x4+x5)
    }
  }

  test("check6 property should succeed") {
    check6((x1:Int,x2:Int,x3:Int,x4:Int,x5:Int,x6:Int) => x1+x2+x3+x4+x5+x6==x1+x2+x3+x4+x5+x6)
  }

  test("check6 property should fail") {
    intercept[AssertionException] {
      check6((x1:Int,x2:Int,x3:Int,x4:Int,x5:Int,x6:Int) => x1+x2+x3+x4+x5+x6!=x1+x2+x3+x4+x5+x6)
    }
  }
}

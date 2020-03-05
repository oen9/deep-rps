package com.github.oen9.deeprps

import zio.test._
import zio.test.Assertion._

object HelloTest extends DefaultRunnableSpec {
  def spec = suite("example test")(
    test("dummy test") {
      assert("hello")(equalTo("hello"))
    }
  )
}

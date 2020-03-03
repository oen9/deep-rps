package example

import zio.test._
import zio.test.Assertion._

object HelloTest extends DefaultRunnableSpec(
  suite("example test")(
    test("dummy test") {
      assert("hello", equalTo("hello"))
    }
  )
)

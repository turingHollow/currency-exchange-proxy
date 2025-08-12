package forex.domain

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import forex.domain.Currency._

final class CurrencySpec extends AnyFunSuite with Matchers {

  test("everySupportedPair should contain all ordered pairs without self-pairs") {
    val pairs = everySupportedPair
    val n = values.size
    pairs.size shouldBe n * (n - 1)

    all(pairs.map(p => p.from != p.to)) shouldBe true

    pairs.toSet.toList.sortBy[String](p => p.from.toString + p.to.toString) shouldBe
      pairs.sortBy[String](p => p.from.toString + p.to.toString)
  }
}

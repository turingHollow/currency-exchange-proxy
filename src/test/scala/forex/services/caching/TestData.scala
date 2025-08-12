package forex.services.caching

import forex.domain.{Currency, Price, Rate, Timestamp}

object TestData {

  val pair1: Rate.Pair = Rate.Pair(Currency.USD, Currency.EUR)
  val pair2: Rate.Pair = Rate.Pair(Currency.EUR, Currency.JPY)

  val nonExistingPair: Rate.Pair = Rate.Pair(Currency.JPY, Currency.USD)

  val rate1: Rate = Rate(pair1, Price(1.1), Timestamp.now)
  val rate2: Rate = Rate(pair2, Price(120.0), Timestamp.now)

  val testData: Map[Rate.Pair, Rate] = Map(
    pair1 -> rate1,
    pair2 -> rate2
  )

}

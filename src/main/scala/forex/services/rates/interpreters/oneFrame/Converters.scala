package forex.services.rates.interpreters.oneFrame
import forex.domain.Rate.Pair
import forex.domain._
import forex.services.rates.interpreters.oneFrame.Protocol.OneFrameRateResponse

object Converters {

  private[oneFrame] implicit class OneFrameRateResponseOps(val resp: OneFrameRateResponse) extends AnyVal {
    def toDomain: Rate =
      Rate(
        pair = Pair(
          from = resp.from,
          to = resp.to
        ),
        price = Price(resp.price),
        timestamp = Timestamp(resp.time_stamp)
      )
  }

}

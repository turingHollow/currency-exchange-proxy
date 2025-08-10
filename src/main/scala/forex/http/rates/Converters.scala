package forex.http.rates

import forex.domain._
import forex.programs.rates.errors.{Error => RatesProgramError}

object Converters {
  import Protocol._

  private[rates] implicit class GetApiResponseOps(val rate: Rate) extends AnyVal {
    def asGetApiResponse: GetApiResponse =
      GetApiResponse(
        from = rate.pair.from,
        to = rate.pair.to,
        price = rate.price,
        timestamp = rate.timestamp
      )
  }

  private[rates] implicit class GetApiErrorOps(val rateError: RatesProgramError) extends AnyVal {
    def asGetApiError: GetApiError =
      rateError match {
        case RatesProgramError.RateLookupFailed(msg) => GetApiError(s"Rate lookup failed with reason: $msg")
      }
  }

}

package forex.programs.rates

import forex.domain.Rate
import forex.services.caching.errors.{Error => CachingServiceError}

import java.time.Instant

object errors {

  sealed trait Error extends Exception

  object Error {
    final case class RateLookupFailed(msg: String) extends Error
    final case class RateLookupOutdated(msg: String, staleData: Rate, lastUpdated: Instant) extends Error

  }

  def toProgramError(error: CachingServiceError): Error = error match {
    case CachingServiceError.CacheMiss(msg)                   => Error.RateLookupFailed(msg)
    case CachingServiceError.StaleData(msg, rate, lastUpdate) => Error.RateLookupOutdated(msg, rate, lastUpdate)
  }
}

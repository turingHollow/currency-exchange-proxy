package forex.programs.rates

import forex.services.caching.errors.{Error => CachingServiceError}

object errors {

  sealed trait Error extends Exception

  object Error {
    final case class RateLookupFailed(msg: String) extends Error
  }

  def toProgramError(error: CachingServiceError): Error = error match {
    case CachingServiceError.CacheMiss(msg) => Error.RateLookupFailed(msg)
  }
}

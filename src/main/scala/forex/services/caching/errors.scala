package forex.services.caching

import forex.domain.Rate

import java.time.Instant

object errors {

  sealed trait Error
  object Error {
    final case class CacheMiss(msg: String) extends Error
    final case class StaleData(msg: String, rate: Rate, lastUpdate: Instant) extends Error
  }

}

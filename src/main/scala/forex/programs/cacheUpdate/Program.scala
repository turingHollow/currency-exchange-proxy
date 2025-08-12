package forex.programs.cacheUpdate

import cats.data.EitherT
import forex.services.{CachingServiceWrites, RatesService}
import fs2.Stream
import cats.effect.Temporal
import forex.config.CachingConfig
import forex.domain.Currency
import forex.services.rates.errors.Error
import org.typelevel.log4cats.LoggerFactory

import scala.concurrent.duration.{DurationInt, DurationLong}

class Program[F[_]: Temporal: LoggerFactory](
    cachingService: CachingServiceWrites[F],
    ratesService: RatesService[F],
    cachingConfig: CachingConfig
) extends Algebra[F] {

  private val logger = LoggerFactory.getLogger[F]

  private val updateRates: F[Error Either Unit] =
    (for {
      maybeData <- EitherT(ratesService.get(Currency.everySupportedPair))
      _ <- EitherT.liftF[F, Error, Unit](cachingService.update(maybeData.map(rate => rate.pair -> rate).toMap))
    } yield ()).value

  override def updatingStream: fs2.Stream[F, Unit] = {
    def handleError(error: Error): fs2.Stream[F, Unit] = {
      val errorMsg = error match {
        case e: Error.OneFrameLookupFailed => s"Can't update rates: ${e.msg}"
      }
      Stream.eval(logger.error(errorMsg)) >>
        Stream.sleep[F](cachingConfig.retryDelayMillis.millis) >>
        updatingStream
    }

    def processUpdate: fs2.Stream[F, Unit] =
      Stream.eval(updateRates).flatMap {
        case Left(error) => handleError(error)
        case Right(_)    => Stream.eval(logger.info("Rates updated successfully"))
      }

    processUpdate >>
      Stream
        .awakeEvery[F](cachingConfig.updateDelayMillis.millis)
        .flatMap(_ => processUpdate)
  }
}

object Program {

  def apply[F[_]: Temporal: LoggerFactory](
      cachingService: CachingServiceWrites[F],
      ratesService: RatesService[F],
      cachingConfig: CachingConfig
  ): Algebra[F] =
    new Program[F](cachingService, ratesService, cachingConfig)
}

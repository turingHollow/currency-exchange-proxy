package forex.programs.cacheUpdate

import cats.data.OptionT
import forex.services.{CachingServiceWrites, RatesService}
import fs2.Stream
import cats.effect.Temporal
import cats.implicits._
import forex.domain.Rate.Pair
import forex.domain.{Currency, Rate}

import scala.concurrent.duration.DurationInt

class Program[F[_]: Temporal](
    cachingService: CachingServiceWrites[F],
    ratesService: RatesService[F]
) extends Algebra[F] {

  private val everySupportedPair: IndexedSeq[Rate.Pair] =
    for {
      a <- Currency.values
      b <- Currency.values if a != b
    } yield Pair(a, b)

  private val updateRates =
    (for {
      maybeData <- OptionT(ratesService.get(everySupportedPair))
      _ <- OptionT.liftF(cachingService.update(maybeData.map(rate => rate.pair -> rate).toMap))
    } yield ()).value.void

  override def updatingStream: fs2.Stream[F, Unit] =
    Stream.eval(updateRates) ++ Stream
      .awakeEvery[F](4.minutes)
      .evalMap(_ => updateRates)
}

object Program {

  def apply[F[_]: Temporal](cachingService: CachingServiceWrites[F], ratesService: RatesService[F]): Algebra[F] =
    new Program[F](cachingService, ratesService)
}

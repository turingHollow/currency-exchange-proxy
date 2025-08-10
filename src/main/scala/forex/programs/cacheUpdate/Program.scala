package forex.programs.cacheUpdate

import forex.services.{CachingServiceWrites, RatesService}
import fs2.Stream
import cats.effect.Temporal
import cats.implicits.toFlatMapOps
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
    ratesService
      .get(everySupportedPair)
      .flatMap(l => cachingService.update(l.map(rate => rate.pair -> rate).toMap))

  override def updatingStream: fs2.Stream[F, Unit] =
    Stream.eval(updateRates) ++ Stream
      .awakeEvery[F](4.minutes)
      .evalMap(_ => updateRates)
}

object Program {

  def apply[F[_]: Temporal](cachingService: CachingServiceWrites[F], ratesService: RatesService[F]): Algebra[F] =
    new Program[F](cachingService, ratesService)
}

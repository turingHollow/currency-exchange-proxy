package forex.services.caching.intepreters

import cats.effect._
import cats.implicits._
import forex.config.CachingConfig
import forex.domain.Rate
import forex.services.caching.Algebra
import forex.services.caching.errors.Error.{CacheMiss, StaleData}
import forex.services.caching.errors._

import java.time.Instant
import scala.concurrent.duration.DurationLong

final class InMemoryCachingService[F[_]: Temporal] private (
    ref: Ref[F, (Map[Rate.Pair, Rate], Instant)],
    ttlMillis: Long
) extends Algebra[F] {

  def get(pair: Rate.Pair): F[Error Either Rate] =
    ref.get.map { case (dataMap, lastUpdated) =>
      val now = Instant.now()
      val isFresh = lastUpdated.minusMillis(ttlMillis).isAfter(now)
      dataMap.get(pair) match {
        case Some(value) if isFresh => Right(value)
        case Some(value)            => Left(StaleData("Data haven't been updated", value, lastUpdated))
        case None                   => Left(CacheMiss("Data not found"))
      }

    }

  override def update(newData: Map[Rate.Pair, Rate]): F[Unit] =
    for {
      ts <- Temporal[F].realTimeInstant
      _ <- ref.set((newData, ts))
    } yield ()
}

object InMemoryCachingService {
  def resource[F[_]: Temporal](cachingConfig: CachingConfig): Resource[F, InMemoryCachingService[F]] =
    for {
      ref <- Resource.eval(
        Ref.of[F, (Map[Rate.Pair, Rate], Instant)]((Map.empty, Instant.EPOCH))
      )
      cache = new InMemoryCachingService[F](ref, cachingConfig.ttlMillis)
    } yield cache
}

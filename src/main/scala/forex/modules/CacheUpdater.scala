package forex.modules

import cats.effect.{Async, Resource}
import forex.config.ApplicationConfig
import forex.programs._
import forex.services._
import fs2.Stream
import fs2.io.net.Network
import org.http4s.client.Client
import org.http4s.ember.client.EmberClientBuilder
import org.typelevel.log4cats.LoggerFactory

class CacheUpdater[F[_]: Async: LoggerFactory](
    config: ApplicationConfig,
    client: Client[F],
    cachingService: CachingServiceWrites[F]
) {

  private val ratesService: RatesService[F] = RatesServices.oneFrame[F](client, config.oneFrame)

  private val cacheProgram: CacheProgram[F] = CacheProgram[F](cachingService, ratesService)

  val stream: Stream[F, Unit] = cacheProgram.updatingStream

}

object CacheUpdater {
  def resource[F[_]: Async: LoggerFactory: Network](
      config: ApplicationConfig,
      cachingService: CachingServiceWrites[F]
  ): Resource[F, CacheUpdater[F]] =
    for {
      client <- EmberClientBuilder.default[F].build
    } yield new CacheUpdater[F](config, client, cachingService)
}

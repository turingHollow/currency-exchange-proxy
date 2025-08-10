package forex.services.rates

import cats.effect.Concurrent
import forex.config.OneFrameConfig
import forex.services.rates.interpreters.oneFrame.OneFrameRatesService
import org.http4s.client.Client

object Interpreters {
  def oneFrame[F[_]: Concurrent](client: Client[F], config: OneFrameConfig): Algebra[F] =
    new OneFrameRatesService[F](client, config)
}

package forex.services.rates

import cats.effect.kernel.Temporal
import org.http4s.client.Client
import forex.config.OneFrameConfig
import forex.services.rates.interpreters.oneFrame.OneFrameRatesService

object Interpreters {
  def oneFrame[F[_]: Temporal](client: Client[F], config: OneFrameConfig): Algebra[F] =
    new OneFrameRatesService[F](client, config)
}

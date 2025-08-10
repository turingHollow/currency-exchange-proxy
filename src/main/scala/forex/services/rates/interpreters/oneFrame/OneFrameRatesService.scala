package forex.services.rates.interpreters.oneFrame

import cats.effect._
import cats.implicits.toFunctorOps
import forex.config.OneFrameConfig
import forex.domain.Rate
import forex.services.rates.Algebra
import forex.services.rates.interpreters.oneFrame.Converters.OneFrameRateResponseOps
import forex.services.rates.interpreters.oneFrame.Protocol.OneFrameRateResponse
import io.circe.generic.auto._
import org.http4s.circe.CirceEntityCodec.circeEntityDecoder
import org.http4s.client.Client

class OneFrameRatesService[F[_]: Concurrent](client: Client[F], config: OneFrameConfig) extends Algebra[F] {

  private val uri = config.url

  def get(pairs: Seq[Rate.Pair]): F[List[Rate]] = {
    val pairsStrings = pairs.map(p => p.from.toString + p.to.toString)
    val uriWithQuery = uri.withQueryParam("pair", pairsStrings)
    client
      .expect[List[OneFrameRateResponse]](uriWithQuery)
      .map(
        _.map(_.toDomain)
      )

  }
}

object OneFrameRatesService {
  def apply[F[_]: Concurrent](
      client: Client[F],
      config: OneFrameConfig
  ) = new OneFrameRatesService(client, config)

}

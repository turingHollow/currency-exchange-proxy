package forex.services.rates.interpreters.oneFrame

import cats.effect._
import cats.implicits._
import forex.config.OneFrameConfig
import forex.domain.Rate
import forex.services.rates.Algebra
import forex.services.rates.interpreters.oneFrame.Converters.OneFrameRateResponseOps
import forex.services.rates.interpreters.oneFrame.Protocol.OneFrameRateResponse
import forex.services.rates.errors.Error
import org.http4s.{Header, Headers, Method, Request}
import org.http4s.circe.CirceEntityCodec.circeEntityDecoder
import org.http4s.client.Client
import org.typelevel.ci.CIString

class OneFrameRatesService[F[_]: Temporal](client: Client[F], config: OneFrameConfig) extends Algebra[F] {

  private val ratesUri = config.url / "rates"

  def get(pairs: Seq[Rate.Pair]): F[Error Either List[Rate]] = {
    val pairsStrings = pairs.map(p => p.from.show + p.to.show)
    val request: Request[F] = Request[F](
      method = Method.GET,
      uri = ratesUri.withQueryParam("pair", pairsStrings),
      headers = Headers(
        Header.Raw(CIString("Token"), s"${config.token}")
      )
    )

    for {
      resultAttempt <- client
        .expect[List[OneFrameRateResponse]](request)
        .attempt
    } yield resultAttempt.leftMap(e => Error.OneFrameLookupFailed(e.toString)) map (_.map(_.toDomain))

  }
}

object OneFrameRatesService {
  def apply[F[_]: Temporal](
      client: Client[F],
      config: OneFrameConfig
  ) = new OneFrameRatesService(client, config)

}

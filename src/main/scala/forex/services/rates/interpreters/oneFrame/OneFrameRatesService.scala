package forex.services.rates.interpreters.oneFrame

import cats.effect._
import cats.implicits._
import forex.config.OneFrameConfig
import forex.domain.Rate
import forex.services.rates.Algebra
import forex.services.rates.interpreters.oneFrame.Converters.OneFrameRateResponseOps
import forex.services.rates.interpreters.oneFrame.Protocol.OneFrameRateResponse
import org.http4s.{Header, Headers, Method, Request}
import org.http4s.circe.CirceEntityCodec.circeEntityDecoder
import org.http4s.client.Client
import org.typelevel.ci.CIString
import org.typelevel.log4cats.LoggerFactory

class OneFrameRatesService[F[_]: Temporal: LoggerFactory](client: Client[F], config: OneFrameConfig)
    extends Algebra[F] {

  private val logger = LoggerFactory.getLogger[F]

  private val ratesUri = config.url / "rates"

  def get(pairs: Seq[Rate.Pair]): F[Option[List[Rate]]] = {
    val pairsStrings = pairs.map(p => p.from.toString + p.to.toString)
    val uriWithQuery = ratesUri.withQueryParam("pair", pairsStrings)
    val request: Request[F] = Request[F](
      method = Method.GET,
      uri = uriWithQuery,
      headers = Headers(
        Header.Raw(CIString("Token"), s"${config.token}")
      )
    )

    for {
      resultAttempt <- client
        .expect[List[OneFrameRateResponse]](request)
        .attempt
      _ <- resultAttempt.fold(
        e => logger.warn(s"Http $request failed with error: $e"),
        result => logger.info(s"Http request succeeded result: $result")
      )
    } yield resultAttempt.toOption.map(_.map(_.toDomain))

  }
}

object OneFrameRatesService {
  def apply[F[_]: Temporal: LoggerFactory](
      client: Client[F],
      config: OneFrameConfig
  ) = new OneFrameRatesService(client, config)

}

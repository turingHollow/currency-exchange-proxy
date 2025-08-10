package forex.http
package rates

import cats.data.NonEmptyList
import cats.effect.Sync
import cats.implicits.catsSyntaxTuple2Semigroupal
import cats.syntax.flatMap._
import forex.programs.RatesProgram
import forex.programs.rates.{Protocol => RatesProgramProtocol}
import org.http4s.{HttpRoutes, ParseFailure}
import org.http4s.dsl.Http4sDsl
import org.http4s.server.Router

class RatesHttpRoutes[F[_]: Sync](rates: RatesProgram[F]) extends Http4sDsl[F] {

  import Converters._, QueryParams._, Protocol._

  private[http] val prefixPath = "/rates"

  private val httpRoutes: HttpRoutes[F] = HttpRoutes.of[F] {
    case GET -> Root :? FromQueryParam(from) +& ToQueryParam(to) =>
      (from, to).tupled.fold(
        (errs: NonEmptyList[ParseFailure]) =>
          BadRequest(
            GetApiError(s"Invalid query parameters: ${errs.toList.map(e => s"${e.sanitized}: ${e.details}")}")
          ),
        { case (from, to) =>
          rates
            .get(RatesProgramProtocol.GetRatesRequest(from, to))
            .flatMap {
              case Left(value)  => NotFound(value.asGetApiError)
              case Right(value) => Ok(value.asGetApiResponse)
            }
        }
      )
  }

  val routes: HttpRoutes[F] = Router(
    prefixPath -> httpRoutes
  )

}

package forex.modules

import cats.effect.Async
import forex.config.ApplicationConfig
import forex.http.rates.RatesHttpRoutes
import forex.programs._
import forex.services._
import org.http4s._
import org.http4s.implicits._
import org.http4s.server.middleware.{AutoSlash, Timeout}

class App[F[_]: Async](config: ApplicationConfig, cachingService: CachingServiceReads[F]) {

  private val ratesProgram: RatesProgram[F] = RatesProgram[F](cachingService)

  private val ratesHttpRoutes: HttpRoutes[F] = new RatesHttpRoutes[F](ratesProgram).routes

  type PartialMiddleware = HttpRoutes[F] => HttpRoutes[F]
  type TotalMiddleware = HttpApp[F] => HttpApp[F]

  private val routesMiddleware: PartialMiddleware = {
    { http: HttpRoutes[F] =>
      AutoSlash(http)
    }
  }

  private val appMiddleware: TotalMiddleware = { http: HttpApp[F] =>
    Timeout(config.http.timeout)(http)
  }

  private val httpRoutes: HttpRoutes[F] = ratesHttpRoutes

  val http: HttpApp[F] = appMiddleware(routesMiddleware(httpRoutes).orNotFound)

}

object App {
  def apply[F[_]: Async](config: ApplicationConfig, cachingService: CachingServiceReads[F]): App[F] =
    new App[F](config, cachingService)
}

package forex

import cats.effect._
import forex.config._
import forex.modules.{CacheUpdater, App}
import forex.services.CachingServices
import fs2.Stream
import fs2.io.net.Network
import org.http4s.blaze.server.BlazeServerBuilder

object Main extends IOApp {
  override def run(args: List[String]): IO[ExitCode] =
    new Application[IO].stream.compile.drain.as(ExitCode.Success)
}

class Application[F[_]: Async: Network] {

  def stream: Stream[F, Unit] =
    for {
      config <- Config.stream("app")
      cachingService <- Stream.resource(CachingServices.inMemory[F])
      app = App[F](config, cachingService)
      cacheUpdater <- Stream.resource(CacheUpdater.resource[F](config, cachingService))
      _ <- BlazeServerBuilder[F]
        .bindHttp(config.http.port, config.http.host)
        .withHttpApp(app.http)
        .serve
        .concurrently(cacheUpdater.stream)
    } yield ()
}

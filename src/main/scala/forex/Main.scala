package forex

import cats.effect._
import forex.config._
import forex.modules.{App, CacheUpdater}
import forex.services.CachingServices
import fs2.Stream
import fs2.io.net.Network
import org.http4s.blaze.server.BlazeServerBuilder
import org.typelevel.log4cats.{LoggerFactory, SelfAwareStructuredLogger}
import org.typelevel.log4cats.slf4j.Slf4jFactory

object Main extends IOApp {
  implicit val logging: LoggerFactory[IO] = Slf4jFactory.create[IO]

  override def run(args: List[String]): IO[ExitCode] =
    new Application[IO].stream.compile.drain.as(ExitCode.Success)
}

class Application[F[_]: Async: Network: LoggerFactory] {

  val logger: SelfAwareStructuredLogger[F] = LoggerFactory[F].getLogger

  def stream: Stream[F, Unit] =
    for {
      config <- Config.stream("app")
      cachingService <- Stream.resource(CachingServices.inMemory[F])
      cacheUpdater <- Stream.resource(CacheUpdater.resource[F](config, cachingService))
      app = App[F](config, cachingService)
      _ <- BlazeServerBuilder[F]
        .bindHttp(config.http.port, config.http.host)
        .withHttpApp(app.http)
        .serve
        .concurrently(cacheUpdater.stream)
        .handleErrorWith(e => Stream.eval(logger.error(e)("Unhandled error")))
    } yield ()
}

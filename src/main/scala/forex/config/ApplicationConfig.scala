package forex.config

import org.http4s.Uri
import pureconfig.ConfigReader
import pureconfig.error.UserValidationFailed

import scala.concurrent.duration.FiniteDuration

case class ApplicationConfig(
    http: HttpConfig,
    oneFrame: OneFrameConfig,
    caching: CachingConfig
)

case class HttpConfig(
    host: String,
    port: Int,
    timeout: FiniteDuration
)

case class OneFrameConfig(
    url: Uri,
    token: String
)

case class CachingConfig(
    ttlMillis: Long,
    updateDelayMillis: Long,
    retryDelayMillis: Long
)

object CachingConfig {
  import pureconfig.generic.semiauto._

  implicit val cachingConfigReader: ConfigReader[CachingConfig] = deriveReader[CachingConfig].emap {
    case s @ CachingConfig(ttl, update, retry) if (update < ttl) && (retry < update) => Right(s)
    case _ => Left(UserValidationFailed("Check fields of CachingConfig"))
  }
}

package forex.services.rates.interpreters.oneFrame

import forex.domain._
import io.circe._
import io.circe.generic.extras.Configuration
import io.circe.generic.extras.semiauto.deriveConfiguredDecoder

import java.time.OffsetDateTime

object Protocol {

  final case class OneFrameRateResponse(
      from: Currency,
      to: Currency,
      price: BigDecimal,
      time_stamp: OffsetDateTime
  )

  implicit val configuration: Configuration = Configuration.default.withSnakeCaseMemberNames

  implicit val responseDecoder: Decoder[OneFrameRateResponse] =
    deriveConfiguredDecoder[OneFrameRateResponse]

}

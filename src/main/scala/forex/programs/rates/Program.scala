package forex.programs.rates

import cats.Functor
import cats.data.EitherT
import forex.domain._
import forex.services.CachingServiceReads
import errors.Error

class Program[F[_]: Functor](
    cachingService: CachingServiceReads[F]
) extends Algebra[F] {

  override def get(request: Protocol.GetRatesRequest): F[Error Either Rate] = {
    EitherT(cachingService.get(Rate.Pair(request.from, request.to)))
      .leftMap(errors.toProgramError)
      .value
  }
}

object Program {

  def apply[F[_]: Functor](
      cachingService: CachingServiceReads[F]
  ): Algebra[F] = new Program[F](cachingService)
}

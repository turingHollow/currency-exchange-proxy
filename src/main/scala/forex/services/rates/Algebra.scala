package forex.services.rates

import forex.domain.Rate

trait Algebra[F[_]] {
  def get(pairs: Seq[Rate.Pair]): F[Option[List[Rate]]]
}

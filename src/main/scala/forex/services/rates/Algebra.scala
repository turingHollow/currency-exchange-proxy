package forex.services.rates

import forex.domain.Rate
import forex.services.rates.errors.Error

trait Algebra[F[_]] {
  def get(pairs: Seq[Rate.Pair]): F[Error Either List[Rate]]
}
